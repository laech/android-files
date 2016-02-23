package l.files.operations;

import java.io.IOException;
import java.util.Collection;

import l.files.fs.Name;
import l.files.fs.Path;

import static l.files.base.Objects.requireNonNull;
import static l.files.operations.Files.getNonExistentDestinationFile;

abstract class Paste extends AbstractOperation {

    private final Path destinationDirectory;

    Paste(Path sourceDirectory, Collection<? extends Name> sourceFiles, Path destinationDirectory) {
        super(sourceDirectory, sourceFiles);
        this.destinationDirectory = requireNonNull(destinationDirectory);
    }

    @Override
    void process(Path sourceDirectory, Name sourceFile) throws InterruptedException {
        checkInterrupt();

        Path path = sourceDirectory.resolve(sourceFile);

        if (destinationDirectory.startsWith(path)) {
            throw new CannotPasteIntoSelfException(
                    "Cannot paste directory " + path +
                            " into its own sub directory " + destinationDirectory
            );
        }

        try {
            Path to = getNonExistentDestinationFile(path, destinationDirectory);
            paste(path, to);
        } catch (IOException e) {
            record(sourceDirectory, sourceFile, e);
        }
    }

    /**
     * Pastes the source to the destination. If {@code from} is a file, write
     * its content into {@code to}. If {@code from} is a directory, paste its
     * content into {@code to}.
     */
    abstract void paste(Path from, Path to) throws IOException;

}
