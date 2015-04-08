package l.files.operations;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;

import l.files.fs.Resource;

import static l.files.fs.Resource.TraversalOrder;

abstract class AbstractOperation implements FileOperation {

    /**
     * The amount of errors to catch before stopping. Don't want to hold an
     * endless amount of errors (resulting in OutOfMemoryError). And there is
     * not much point of continuing if number of errors reached this amount.
     */
    private static final int ERROR_LIMIT = 20;

    private final Iterable<Resource> resources;

    AbstractOperation(Iterable<? extends Resource> resources) {
        this.resources = ImmutableSet.copyOf(resources);
    }

    void checkInterrupt() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    @Override
    public void execute() throws InterruptedException {
        FailureRecorder listener = new FailureRecorder(ERROR_LIMIT);
        for (Resource resource : resources) {
            checkInterrupt();
            process(resource, listener);
        }
        listener.throwIfNotEmpty();
    }

    abstract void process(Resource resource, FailureRecorder listener)
            throws InterruptedException;

    protected final Resource.Stream traverse(
            Resource resource,
            TraversalOrder order,
            final FailureRecorder listener) throws IOException {

        return resource.traverse(order, new Resource.TraversalExceptionHandler() {
            @Override
            public void handle(Resource resource, IOException e) {
                listener.onFailure(resource, e);
            }
        });
    }

}
