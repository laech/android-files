package l.files.fs.exception;

import java.io.IOException;

public final class FileSystemReadOnly extends IOException {

    public FileSystemReadOnly(String message, Throwable cause) {
        super(message, cause);
    }

}
