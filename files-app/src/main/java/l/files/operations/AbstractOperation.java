package l.files.operations;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.Resource;

import static l.files.fs.Resource.TraversalOrder;

abstract class AbstractOperation implements FileOperation {

    /**
     * The amount of errors to catch before stopping. Don't want to hold an
     * endless amount of errors (resulting in OutOfMemoryError). And there is
     * not much point of continuing if number of errors reached this amount.
     */
    private static final int ERROR_LIMIT = 20;

    private final Iterable<Path> paths;

    AbstractOperation(Iterable<? extends Path> paths) {
        this.paths = ImmutableSet.copyOf(paths);
    }

    void checkInterrupt() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    @Override
    public void execute() throws InterruptedException {
        FailureRecorder listener = new FailureRecorder(ERROR_LIMIT);
        for (Path path : paths) {
            checkInterrupt();
            process(path, listener);
        }
        listener.throwIfNotEmpty();
    }

    abstract void process(Path path, FailureRecorder listener)
            throws InterruptedException;

    protected final Resource.ResourceStream traverse(Path path, TraversalOrder order, final FailureRecorder listener) throws IOException {
        return path.getResource().traverse(order, new Resource.TraversalExceptionHandler() {
            @Override
            public void handle(Resource resource, IOException e) {
                listener.onFailure(resource.getPath(), e);
            }
        });
    }

}
