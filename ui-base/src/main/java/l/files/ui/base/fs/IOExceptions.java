package l.files.ui.base.fs;

import java.io.IOException;

import linux.ErrnoException;

public final class IOExceptions {

    private IOExceptions() {
    }

    public static String message(IOException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof ErrnoException) {
            return cause.getMessage();
        }
        return exception.getMessage();
    }

}
