package l.files.operations;

import java.io.IOException;
import java.util.Collection;

import l.files.fs.File;

import static java.util.Objects.requireNonNull;
import static l.files.operations.Files.getNonExistentDestinationFile;

abstract class Paste extends AbstractOperation {

    private final File destination;

    Paste(Collection<? extends File> files, File destination) {
        super(files);
        this.destination = requireNonNull(destination, "destination");
    }

    @Override
    void process(File file) throws InterruptedException {
        checkInterrupt();

        if (destination.equals(file) || destination.pathStartsWith(file)) {
            throw new CannotPasteIntoSelfException(
                    "Cannot paste directory " + file +
                            " into its own sub directory " + destination
            );
        }

        try {
            File to = getNonExistentDestinationFile(file, destination);
            paste(file, to);
        } catch (IOException e) {
            record(file, e);
        }
    }

    /**
     * Pastes the source to the destination. If {@code from} is a file, write
     * its content into {@code to}. If {@code from} is a directory, paste its
     * content into {@code to}.
     */
    abstract void paste(File from, File to) throws IOException;

}
