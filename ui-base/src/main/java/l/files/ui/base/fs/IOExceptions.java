package l.files.ui.base.fs;

import java.io.IOException;

import l.files.fs.local.ErrnoException;

public final class IOExceptions {

    private IOExceptions() {
    }

    public static String message(final IOException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof ErrnoException) {
            return ((ErrnoException) cause).strerror();
        }
        return exception.getMessage();
    }

}
