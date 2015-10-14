package l.files.ui.base.fs;

import android.system.ErrnoException;

import java.io.IOException;

import static android.system.Os.strerror;

public final class IOExceptions {

    private IOExceptions() {
    }

    public static String message(final IOException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof ErrnoException) {
            return strerror(((ErrnoException) cause).errno);
        }
        return exception.getMessage();
    }

}
