package l.files.fs.exception;

import java.io.FileNotFoundException;

public final class NoSuchEntry extends FileNotFoundException {

    public NoSuchEntry(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

}
