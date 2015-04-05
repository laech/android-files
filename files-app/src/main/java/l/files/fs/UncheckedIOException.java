package l.files.fs;

import java.io.IOException;

public class UncheckedIOException extends RuntimeException {

    public UncheckedIOException(IOException throwable) {
        super(throwable);
    }

}
