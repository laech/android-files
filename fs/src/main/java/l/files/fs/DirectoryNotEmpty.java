package l.files.fs;

import java.io.IOException;

public final class DirectoryNotEmpty extends IOException {

    public DirectoryNotEmpty(String message, Throwable cause) {
        super(message, cause);
    }

}
