package l.files.operations;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static java.util.Objects.requireNonNull;
import static l.files.fs.PathKt.getNonExistentDestinationFile;

abstract class Paste extends AbstractOperation {

    private final Path destinationDir;

    Paste(Collection<? extends Path> sourcePaths, Path destinationDir) {
        super(sourcePaths);
        this.destinationDir = requireNonNull(destinationDir, "destinationDir");
    }

    @Override
    void process(Path sourcePath) throws InterruptedException {
        checkInterrupt();

        if (destinationDir.startsWith(sourcePath)) {
            throw new CannotPasteIntoSelfException(
                "Cannot paste directory " + sourcePath +
                    " into its own sub directory " + destinationDir);
        }

        try {
            Path destinationPath =
                getNonExistentDestinationFile(sourcePath, destinationDir);
            paste(sourcePath, destinationPath);
        } catch (IOException e) {
            record(sourcePath, e);
        }
    }

    /**
     * Pastes the source to the destination. If {@code sourcePath} is a file,
     * write its content into {@code destinationPath}. If {@code sourcePath}
     * is a directory, paste its content into {@code destinationPath}.
     */
    abstract void paste(Path sourcePath, Path destinationPath)
        throws IOException;
}
