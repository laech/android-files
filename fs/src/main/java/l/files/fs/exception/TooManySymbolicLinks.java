package l.files.fs.exception;

import java.io.IOException;

public final class TooManySymbolicLinks extends IOException {

    public TooManySymbolicLinks(String message, Throwable cause) {
        super(message, cause);
    }

}
