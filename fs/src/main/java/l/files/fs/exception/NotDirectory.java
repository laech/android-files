package l.files.fs.exception;

import java.io.IOException;

public final class NotDirectory extends IOException {

    public NotDirectory(String message, Throwable cause) {
        super(message, cause);
    }

}
