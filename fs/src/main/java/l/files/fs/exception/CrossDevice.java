package l.files.fs.exception;

import java.io.IOException;

public final class CrossDevice extends IOException {

    public CrossDevice(String message, Throwable cause) {
        super(message, cause);
    }

}
