package l.files.fs.exception;

import java.io.IOException;

public final class AccessDenied extends IOException {

    public AccessDenied(String message, Throwable cause) {
        super(message, cause);
    }

}
