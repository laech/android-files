package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.TraversalCallback;

import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableSet;
import static l.files.base.Objects.requireNonNull;
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

    private final Path sourceDirectory;
    private final Iterable<Name> sourceFiles;
    private final FailureRecorder recorder;

    AbstractOperation(Path sourceDirectory, Collection<? extends Name> sourceFiles) {
        this.sourceDirectory = requireNonNull(sourceDirectory);
        this.sourceFiles = unmodifiableSet(new HashSet<>(sourceFiles));
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

    final void record(Path parentDirectory, Name file, final IOException exception) {
        recorder.onFailure(parentDirectory, file, exception);
    }

    final void traverse(Path parentDirectory, Name file, OperationVisitor visitor) {
        try {
            l.files.fs.Files.traverse(parentDirectory.resolve(file), NOFOLLOW, visitor);
        } catch (IOException e) {
            record(parentDirectory, file, e);
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
            record(path.parent(), path.name(), e);
        }

    }

    @Override
    public void execute() throws InterruptedException {
        for (Name file : sourceFiles) {
            checkInterrupt();
            process(sourceDirectory, file);
        }
        recorder.throwIfNotEmpty();
    }

    abstract void process(Path sourceDirectory, Name sourceFile) throws InterruptedException;

}
