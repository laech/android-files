package l.files.fs.exception;

import java.io.IOException;

public final class InvalidArgument extends IOException {

    public InvalidArgument(String message, Throwable cause) {
        super(message, cause);
    }

}
