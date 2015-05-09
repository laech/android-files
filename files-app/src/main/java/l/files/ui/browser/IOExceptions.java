package l.files.ui.browser;

import android.system.ErrnoException;
import android.system.OsConstants;

import java.io.IOException;

final class IOExceptions
{

    private IOExceptions()
    {
    }

    static String getFailureMessage(final IOException exception)
    {
        final Throwable cause = exception.getCause();
        if (cause instanceof android.system.ErrnoException)
        {
            return OsConstants.errnoName(((ErrnoException) cause).errno);
        }
        return exception.getMessage();
    }

}
