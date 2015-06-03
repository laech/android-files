package l.files.ui;

import android.system.ErrnoException;

import java.io.IOException;

import static l.files.fs.local.ErrnoExceptions.strerror;

public final class IOExceptions
{

    private IOExceptions()
    {
    }

    public static String message(final IOException exception)
    {
        final Throwable cause = exception.getCause();
        if (cause instanceof ErrnoException)
        {
            return strerror(((ErrnoException) cause).errno);
        }
        return exception.getMessage();
    }

}
