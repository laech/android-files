package l.files.fs;

import java.io.IOException;

public final class AlreadyExist extends IOException {

    public AlreadyExist(String message, Throwable cause) {
        super(message, cause);
    }

}
