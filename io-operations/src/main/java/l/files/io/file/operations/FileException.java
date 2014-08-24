package l.files.io.file.operations;

import android.os.Build;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static l.files.io.file.operations.FileOperation.Failure;

/**
 * Indicates one or more failures occurred during a {@link FileOperation}.
 */
public final class FileException extends IOException {

    private final List<Failure> failures;

    public FileException(List<Failure> failures) {
        this.failures = ImmutableList.copyOf(failures);
        checkArgument(!this.failures.isEmpty());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (Failure failure : failures) {
                addSuppressed(failure.cause());
            }
        } else {
            initCause(failures.get(0).cause());
        }
    }

    public static void throwIfNotEmpty(List<Failure> failures) throws FileException {
        if (!failures.isEmpty()) {
            throw new FileException(failures);
        }
    }

    public List<Failure> failures() {
        return failures;
    }
}
