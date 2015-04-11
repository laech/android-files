package l.files.logging;

import android.util.Log;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;
import static android.util.Log.isLoggable;
import static java.lang.String.format;

public final class Logger {

    private final String tag;

    private Logger(String tag) {
        this.tag = tag;
    }

    public static Logger get(Class<?> target) {
        return new Logger(target.getSimpleName());
    }

    public void verbose(Object msg) {
        if (isLoggable(tag, VERBOSE)) {
            Log.v(tag, String.valueOf(msg));
        }
    }

    public void verbose(String format, Object arg1) {
        if (isLoggable(tag, VERBOSE)) {
            Log.v(tag, format(format, arg1));
        }
    }

    public void verbose(String format, Object arg1, Object arg2) {
        if (isLoggable(tag, VERBOSE)) {
            Log.v(tag, format(format, arg1, arg2));
        }
    }

    public void verbose(String format, Object arg1, Object arg2, Object arg3) {
        if (isLoggable(tag, VERBOSE)) {
            Log.v(tag, format(format, arg1, arg2, arg3));
        }
    }

    public void verbose(String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        if (isLoggable(tag, VERBOSE)) {
            Log.v(tag, format(format, arg1, arg2, arg3, arg4, arg5));
        }
    }

    public void verbose(String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        if (isLoggable(tag, VERBOSE)) {
            Log.v(tag, format(format, arg1, arg2, arg3, arg4, arg5, arg6));
        }
    }

    public void verbose(String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
        if (isLoggable(tag, VERBOSE)) {
            Log.v(tag, format(format, arg1, arg2, arg3, arg4, arg5, arg6, arg7));
        }
    }

    public void debug(Throwable e, String format, Object arg) {
        if (isLoggable(tag, DEBUG)) {
            Log.d(tag, format(format, arg), e);
        }
    }

    public void debug(String message) {
        debug(message, null);
    }

    public void debug(String format, Object arg) {
        debug(format, arg, null);
    }

    public void debug(String format, Object arg1, Object arg2) {
        debug(format, arg1, arg2, null);
    }

    public void debug(String format, Object arg1, Object arg2, Object arg3) {
        debug(format, arg1, arg2, arg3, null);
    }

    public void debug(String format, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (isLoggable(tag, DEBUG)) {
            Log.d(tag, format(format, arg1, arg2, arg3, arg4));
        }
    }

    public void debug(Throwable e) {
        if (isLoggable(tag, DEBUG)) {
            Log.d(tag, e.getMessage(), e);
        }
    }

    public void warn(Throwable err, String format, Object arg) {
        if (isLoggable(tag, WARN)) {
            Log.w(tag, format(format, arg), err);
        }
    }

    public void warn(Throwable err) {
        if (isLoggable(tag, WARN)) {
            Log.w(tag, err);
        }
    }

    public void warn(String message) {
        if (isLoggable(tag, WARN)) {
            Log.w(tag, message);
        }
    }

    public void error(Throwable e) {
        if (isLoggable(tag, ERROR)) {
            Log.e(tag, e.getMessage(), e);
        }
    }

    public void error(Throwable e, String format, Object arg) {
        if (isLoggable(tag, ERROR)) {
            Log.e(tag, format(format, arg), e);
        }
    }

    public void error(String message) {
        if (isLoggable(tag, ERROR)) {
            Log.e(tag, message);
        }
    }
}
