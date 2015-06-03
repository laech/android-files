package l.files.ui;

import android.system.ErrnoException;
import android.system.OsConstants;

import java.io.IOException;

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
            // TODO use message instead of name
            return OsConstants.errnoName(((ErrnoException) cause).errno);
        }
        return exception.getMessage();
    }

}
