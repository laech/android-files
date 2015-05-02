package l.files.logging;

import android.util.Log;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.WARN;
import static android.util.Log.isLoggable;
import static java.lang.String.format;

public final class Logger
{
    private final String tag;

    private Logger(final String tag)
    {
        this.tag = tag;
    }

    public static Logger get(final Class<?> target)
    {
        return new Logger(target.getSimpleName());
    }

    public boolean isVerboseEnabled()
    {
        return Log.isLoggable(tag, Log.VERBOSE);
    }

    public void verbose(final Object msg)
    {
        verbose("%s", msg);
    }

    public void verbose(final String format, final Object arg1)
    {
        verbose(format, arg1, null);
    }

    public void verbose(
            final String format,
            final Object arg1,
            final Object arg2)
    {
        verbose(format, arg1, arg2, null);
    }

    public void verbose(
            final String format,
            final Object arg1,
            final Object arg2,
            final Object arg3)
    {
        verbose(format, arg1, arg2, arg3, null);
    }

    public void verbose(
            final String format,
            final Object arg1,
            final Object arg2,
            final Object arg3,
            final Object arg4)
    {
        verbose(format, arg1, arg2, arg3, arg4, null);
    }

    public void verbose(
            final String format,
            final Object arg1,
            final Object arg2,
            final Object arg3,
            final Object arg4,
            final Object arg5)
    {
        if (isVerboseEnabled())
        {
            Log.v(tag, format(format, arg1, arg2, arg3, arg4, arg5));
        }
    }

    public void debug(final Throwable e, final String format, final Object arg)
    {
        if (isLoggable(tag, DEBUG))
        {
            Log.d(tag, format(format, arg), e);
        }
    }

    public void debug(final String message)
    {
        debug(message, null);
    }

    public void debug(final String format, final Object arg)
    {
        debug(format, arg, null);
    }

    public void debug(final String format, final Object arg1, final Object arg2)
    {
        debug(format, arg1, arg2, null);
    }

    public void debug(
            final String format,
            final Object arg1,
            final Object arg2,
            final Object arg3)
    {
        debug(format, arg1, arg2, arg3, null);
    }

    public void debug(
            final String format,
            final Object arg1,
            final Object arg2,
            final Object arg3,
            final Object arg4)
    {
        if (isLoggable(tag, DEBUG))
        {
            Log.d(tag, format(format, arg1, arg2, arg3, arg4));
        }
    }

    public void debug(final Throwable e)
    {
        if (isLoggable(tag, DEBUG))
        {
            Log.d(tag, e.getMessage(), e);
        }
    }

    public void warn(final Throwable err, final String format, final Object arg)
    {
        if (isLoggable(tag, WARN))
        {
            Log.w(tag, format(format, arg), err);
        }
    }

    public void warn(final Throwable err)
    {
        if (isLoggable(tag, WARN))
        {
            Log.w(tag, err);
        }
    }

    public void warn(final String message)
    {
        if (isLoggable(tag, WARN))
        {
            Log.w(tag, message);
        }
    }

    public void error(final Throwable e)
    {
        if (isLoggable(tag, ERROR))
        {
            Log.e(tag, e.getMessage(), e);
        }
    }

    public void error(final Throwable e, final String format, final Object arg)
    {
        if (isLoggable(tag, ERROR))
        {
            Log.e(tag, format(format, arg), e);
        }
    }

    public void error(final String message)
    {
        if (isLoggable(tag, ERROR))
        {
            Log.e(tag, message);
        }
    }
}
