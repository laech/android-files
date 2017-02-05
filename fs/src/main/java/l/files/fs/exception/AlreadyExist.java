package l.files.fs.exception;

import java.io.IOException;

public final class AlreadyExist extends IOException {

    public AlreadyExist(String message, Throwable cause) {
        super(message, cause);
    }

}
