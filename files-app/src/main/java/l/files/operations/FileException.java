package l.files.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.unmodifiableList;

/**
 * Indicates one or more failures occurred during a {@link FileOperation}.
 */
final class FileException extends RuntimeException {

    private final List<Failure> failures;

    FileException(Collection<Failure> failures) {
        this.failures = unmodifiableList(new ArrayList<>(failures));
        checkArgument(!this.failures.isEmpty());

        for (Failure failure : failures) {
            addSuppressed(failure.getCause());
        }
    }

    public static void throwIfNotEmpty(Collection<Failure> failures)
            throws FileException {
        if (!failures.isEmpty()) {
            throw new FileException(failures);
        }
    }

    public List<Failure> failures() {
        return failures;
    }
}
