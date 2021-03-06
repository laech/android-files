package l.files.operations;

import java.io.IOException;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;


public final class Failure {

    private final Path path;
    private final IOException cause;

    private Failure(Path path, IOException cause) {
        this.path = requireNonNull(path);
        this.cause = requireNonNull(cause);
    }

    public Path path() {
        return path;
    }

    public IOException cause() {
        return cause;
    }

    public static Failure create(Path path, IOException cause) {
        return new Failure(path, cause);
    }

}
