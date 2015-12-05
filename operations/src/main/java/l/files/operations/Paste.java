package l.files.operations;

import java.io.IOException;
import java.util.Collection;

import l.files.fs.Path;

import static l.files.base.Objects.requireNonNull;
import static l.files.operations.Files.getNonExistentDestinationFile;

abstract class Paste extends AbstractOperation {

    private final Path destination;

    Paste(Collection<? extends Path> files, Path destination) {
        super(files);
        this.destination = requireNonNull(destination, "destination");
    }

    @Override
    void process(Path path) throws InterruptedException {
        checkInterrupt();

        if (destination.startsWith(path)) {
            throw new CannotPasteIntoSelfException(
                    "Cannot paste directory " + path +
                            " into its own sub directory " + destination
            );
        }

        try {
            Path to = getNonExistentDestinationFile(path, destination);
            paste(path, to);
        } catch (IOException e) {
            record(path, e);
        }
    }

    /**
     * Pastes the source to the destination. If {@code from} is a file, write
     * its content into {@code to}. If {@code from} is a directory, paste its
     * content into {@code to}.
     */
    abstract void paste(Path from, Path to) throws IOException;

}
