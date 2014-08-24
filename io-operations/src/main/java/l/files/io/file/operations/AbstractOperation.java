package l.files.io.file.operations;

import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;

import static l.files.io.file.operations.FileException.throwIfNotEmpty;
import static l.files.io.file.operations.FileOperations.checkInterrupt;

public abstract class AbstractOperation implements FileOperation {

    private final Iterable<String> paths;
    private volatile String currentPath;
    private volatile boolean done;

    protected AbstractOperation(Iterable<String> paths) {
        this.paths = ImmutableSet.copyOf(paths);
    }

    /**
     * Gets the current path (one of the paths used to construct this instance) being processed.
     */
    public String getCurrentPath() {
        return currentPath;
    }

    @Override
    public void execute() throws FileException, InterruptedException {
        List<Failure> failures = new ArrayList<>(0);
        try {

            doCall(failures);
            throwIfNotEmpty(failures);

        } finally {
            currentPath = null;
            done = true;
        }

    }

    public void doCall(List<Failure> failures) throws InterruptedException {
        for (String path : paths) {
            checkInterrupt();
            currentPath = path;
            process(path, failures);
        }
    }

    /**
     * Process the given path and its children, collects any failures.
     */
    protected abstract void process(String path, List<Failure> failures)
            throws InterruptedException;

    @Override
    public boolean isDone() {
        return done;
    }
}
