package l.files.fs.exception;

import java.io.IOException;

public final class NameTooLong extends IOException {

    public NameTooLong(String message, Throwable cause) {
        super(message, cause);
    }

}
