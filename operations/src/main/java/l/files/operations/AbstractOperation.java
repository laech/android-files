package l.files.operations;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.util.Set;

import l.files.fs.Path;
import l.files.fs.TraversalCallback;

import static java.lang.Thread.currentThread;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.TraversalCallback.Result.CONTINUE;
import static l.files.fs.TraversalCallback.Result.TERMINATE;

abstract class AbstractOperation implements FileOperation {

    /**
     * The amount of errors to catch before stopping. Don't want to hold an
     * endless amount of errors (resulting in OutOfMemoryError). And there is
     * not much point of continuing if number of errors reached this amount.
     */
    private static final int ERROR_LIMIT = 20;

    private final Set<Path> paths;
    private final FailureRecorder recorder;

    AbstractOperation(Set<? extends Path> paths) {
        this.paths = ImmutableSet.copyOf(paths);
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

    final void record(Path path, final IOException exception) {
        recorder.onFailure(path, exception);
    }

    final void traverse(Path path, OperationVisitor visitor) {
        try {
            path.traverse(NOFOLLOW, visitor);
        } catch (IOException e) {
            record(path, e);
        }
    }

    class OperationVisitor implements TraversalCallback<Path> {

        @Override
        public Result onPreVisit(Path path) throws IOException {
            return isInterrupted() ? TERMINATE : CONTINUE;
        }

        @Override
        public Result onPostVisit(Path path) throws IOException {
            return isInterrupted() ? TERMINATE : CONTINUE;
        }

        @Override
        public void onException(Path path, IOException e) throws IOException {
            record(path, e);
        }

    }

    @Override
    public void execute() throws InterruptedException {
        for (Path path : paths) {
            process(path);
        }
        recorder.throwIfNotEmpty();
    }

    abstract void process(Path path) throws InterruptedException;

}
