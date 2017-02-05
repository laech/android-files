package l.files.fs.exception;

import java.io.IOException;

public final class PermissionDenied extends IOException {

    public PermissionDenied(String message, Throwable cause) {
        super(message, cause);
    }

}
