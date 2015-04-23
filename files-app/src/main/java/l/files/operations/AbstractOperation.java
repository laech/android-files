package l.files.operations;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;

import l.files.fs.Resource;
import l.files.fs.ResourceExceptionHandler;
import l.files.fs.ResourceVisitor;

import static java.lang.Thread.currentThread;
import static l.files.fs.ResourceVisitor.Result.CONTINUE;
import static l.files.fs.ResourceVisitor.Result.TERMINATE;

abstract class AbstractOperation implements FileOperation {

    /**
     * The amount of errors to catch before stopping. Don't want to hold an
     * endless amount of errors (resulting in OutOfMemoryError). And there is
     * not much point of continuing if number of errors reached this amount.
     */
    private static final int ERROR_LIMIT = 20;

    private final Iterable<Resource> resources;
    private final FailureRecorder recorder;

    AbstractOperation(Iterable<? extends Resource> resources) {
        this.resources = ImmutableSet.copyOf(resources);
        this.recorder = new FailureRecorder(ERROR_LIMIT);
    }

    final void checkInterrupt() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    final boolean isInterrupted() {
        return currentThread().isInterrupted();
    }

    final void record(Resource resource, IOException exception) {
        recorder.onFailure(resource, exception);
    }

    final void preOrderTraversal(Resource resource, ResourceVisitor visitor)
            throws IOException {
        resource.traverse(visitor, terminateOnInterrupt(), recordOnException());
    }

    final void postOrderTraversal(Resource resource, ResourceVisitor visitor)
            throws IOException {
        resource.traverse(terminateOnInterrupt(), visitor, recordOnException());
    }

    private ResourceVisitor terminateOnInterrupt() {
        return new ResourceVisitor() {
            @Override
            public Result accept(Resource resource) throws IOException {
                if (isInterrupted()) {
                    return TERMINATE;
                }
                return CONTINUE;
            }
        };
    }

    private ResourceExceptionHandler recordOnException() {
        return new ResourceExceptionHandler() {
            @Override
            public void handle(Resource resource, IOException e) throws IOException {
                record(resource, e);
            }
        };
    }

    @Override
    public void execute() throws InterruptedException {
        FailureRecorder listener = new FailureRecorder(ERROR_LIMIT);
        for (Resource resource : resources) {
            checkInterrupt();
            process(resource);
        }
        listener.throwIfNotEmpty();
    }

    abstract void process(Resource resource) throws InterruptedException;

}
