package l.files.fs.exception;

import java.io.IOException;

public final class IsDirectory extends IOException {

    public IsDirectory(String message, Throwable cause) {
        super(message, cause);
    }

}
