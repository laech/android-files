package l.files.operations;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;

import l.files.fs.Resource;
import l.files.fs.ResourceVisitor.ExceptionHandler;
import l.files.fs.ResourceVisitor.Order;

import static java.lang.Thread.currentThread;

abstract class AbstractOperation implements FileOperation, ExceptionHandler {

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

    @Override
    public void execute() throws InterruptedException {
        FailureRecorder listener = new FailureRecorder(ERROR_LIMIT);
        for (Resource resource : resources) {
            checkInterrupt();
            process(resource);
        }
        listener.throwIfNotEmpty();
    }

    @Override
    public void handle(Order order, Resource resource, IOException e) {
        record(resource, e);
    }

    abstract void process(Resource resource) throws InterruptedException;

}
