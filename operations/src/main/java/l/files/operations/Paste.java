package l.files.operations;

import java.io.IOException;
import java.util.Collection;

import l.files.fs.File;
import l.files.fs.local.Files;
import l.files.fs.local.LocalFile;

import static java.util.Objects.requireNonNull;

abstract class Paste extends AbstractOperation {

    private final File destination;

    Paste(Collection<? extends File> files, File destination) {
        super(files);
        this.destination = requireNonNull(destination, "destination");
    }

    @Override
    void process(File file) throws InterruptedException {
        checkInterrupt();

        java.io.File destinationFile = new java.io.File(destination.uri());
        java.io.File fromFile = new java.io.File(file.uri());
        if (destination.equals(file) || destination.pathStartsWith(file)) {
            // TODO prevent this from UI
            throw new CannotPasteIntoSelfException(
                    "Cannot paste directory " + file +
                            " into its own sub directory " + destination
            );
        }

        java.io.File to = Files.getNonExistentDestinationFile(fromFile, destinationFile);
        try {
            paste(file, LocalFile.create(to));
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
