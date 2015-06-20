package l.files.logging;

import android.util.Log;

import java.util.Objects;

import l.files.BuildConfig;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;
import static android.util.Log.isLoggable;
import static java.lang.String.format;

public final class Logger
{
    private final String tag;

    private Logger(final String tag)
    {
        this.tag = "blah-" + tag;
    }

    public static Logger get(final Class<?> target)
    {
        return new Logger(target.getSimpleName());
    }

    public boolean isVerboseEnabled()
    {
        return BuildConfig.DEBUG && isLoggable(tag, VERBOSE);
    }

    public boolean isDebugEnabled()
    {
        return BuildConfig.DEBUG && isLoggable(tag, DEBUG);
    }

    public boolean isWarnEnabled()
    {
        return BuildConfig.DEBUG && isLoggable(tag, WARN);
    }

    public boolean isErrorEnabled()
    {
        return BuildConfig.DEBUG && isLoggable(tag, ERROR);
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

    public void debug(final Throwable e)
    {
        debug(e, "");
    }

    public void debug(final Throwable e, final Object message)
    {
        debug(e, "%s", message);
    }

    public void debug(
            final Throwable e,
            final String format,
            final Object arg)
    {
        if (isDebugEnabled())
        {
            Log.d(tag, format(format, arg), e);
        }
    }

    public void debug(final Object message)
    {
        debug("%s", message);
    }

    public void debug(final String format, final Object arg)
    {
        debug(format, arg, null);
    }

    public void debug(
            final String format,
            final Object arg1,
            final Object arg2)
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
        if (isDebugEnabled())
        {
            Log.d(tag, format(format, arg1, arg2, arg3, arg4));
        }
    }

    public void warn(final Throwable e)
    {
        warn(e, "");
    }

    public void warn(final Throwable e, final Object message)
    {
        warn(e, "%s", message);
    }

    public void warn(
            final Throwable err,
            final String format,
            final Object arg)
    {
        if (isWarnEnabled())
        {
            Log.w(tag, format(format, arg), err);
        }
    }

    public void warn(final String message)
    {
        if (isWarnEnabled())
        {
            Log.w(tag, message);
        }
    }

    public void error(final Throwable e)
    {
        error(e, "");
    }

    public void error(final Throwable e, final Object message)
    {
        error(e, "%s", message);
    }

    public void error(
            final Throwable e,
            final String format,
            final Object arg)
    {
        if (isErrorEnabled())
        {
            Log.e(tag, format(format, arg), e);
        }
    }

    public void error(final Object message)
    {
        if (isErrorEnabled())
        {
            Log.e(tag, Objects.toString(message));
        }
    }
}
