package l.files.ui.base.fs;

import java.io.IOException;

import l.files.fs.IOExceptionReason;

public final class IOExceptions {

    private IOExceptions() {
    }

    public static String message(IOException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof IOExceptionReason) {
            return ((IOExceptionReason) cause).reason();
        }
        return exception.getMessage();
    }

}
