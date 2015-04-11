package l.files.operations;

import java.io.File;

import l.files.fs.Resource;
import l.files.fs.local.Files;
import l.files.fs.local.LocalResource;

import static java.util.Objects.requireNonNull;

abstract class Paste extends AbstractOperation {

    private final Resource destination;

    Paste(Iterable<? extends Resource> resources, Resource destination) {
        super(resources);
        this.destination = requireNonNull(destination, "destination");
    }

    @Override
    void process(Resource resource, FailureRecorder listener)
            throws InterruptedException {
        checkInterrupt();

        File destinationFile = new File(destination.getUri());
        File fromFile = new File(resource.getUri());
        if (destination.equals(resource) || destination.startsWith(resource)) {
            // TODO prevent this from UI
            throw new CannotPasteIntoSelfException(
                    "Cannot paste directory " + resource +
                            " into its own sub directory " + destination
            );
        }

        File to = Files.getNonExistentDestinationFile(fromFile, destinationFile);
        paste(resource, LocalResource.create(to), listener);
    }

    /**
     * Pastes the source to the destination. If {@code from} is a file, write
     * its content into {@code to}. If {@code from} is a directory, paste its
     * content into {@code to}.
     */
    abstract void paste(Resource from, Resource to, FailureRecorder listener)
            throws InterruptedException;

}
