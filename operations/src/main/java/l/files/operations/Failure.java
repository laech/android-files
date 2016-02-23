package l.files.operations;

import java.io.IOException;

import l.files.fs.Name;
import l.files.fs.Path;

import static l.files.base.Objects.requireNonNull;

public final class Failure {

    private final Path parentDirectory;
    private final Name file;
    private final IOException cause;

    private Failure(Path parentDirectory, Name file, IOException cause) {
        this.parentDirectory = requireNonNull(parentDirectory);
        this.file = requireNonNull(file);
        this.cause = requireNonNull(cause);
    }

    public Path parentDirectory() {
        return parentDirectory;
    }

    public Name file() {
        return file;
    }

    public IOException cause() {
        return cause;
    }

    public static Failure create(Path parentDirectory, Name file, IOException cause) {
        return new Failure(parentDirectory, file, cause);
    }

}
