package l.files.operations;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Thread.currentThread;
import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.unmodifiableSet;

abstract class AbstractOperation implements FileOperation {

    /**
     * The amount of errors to catch before stopping. Don't want to hold an
     * endless amount of errors (resulting in OutOfMemoryError). And there is
     * not much point of continuing if number of errors reached this amount.
     */
    private static final int ERROR_LIMIT = 20;

    private final Set<Path> paths;
    private final FailureRecorder recorder;

    AbstractOperation(Collection<? extends Path> paths) {
        this.paths = unmodifiableSet(new HashSet<>(paths));
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

    final void record(Path path, IOException exception) {
        recorder.onFailure(path, exception);
    }

    final void traverse(Path path, OperationVisitor visitor) {
        try {
            walkFileTree(path, visitor);
        } catch (IOException e) {
            record(path, e);
        }
    }

    class OperationVisitor implements FileVisitor<Path> {

        private FileVisitResult terminateOrContinue() {
            return isInterrupted()
                ? FileVisitResult.TERMINATE
                : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(
            Path dir,
            BasicFileAttributes attrs
        ) throws IOException {
            return terminateOrContinue();
        }

        @Override
        public FileVisitResult visitFile(
            Path file,
            BasicFileAttributes attrs
        ) throws IOException {
            return terminateOrContinue();
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            record(file, e);
            return terminateOrContinue();
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e)
            throws IOException {
            if (e != null) {
                record(dir, e);
            }
            return terminateOrContinue();
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
