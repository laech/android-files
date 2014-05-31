package l.files.logging;

import android.util.Log;

import java.io.IOException;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
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

  public void warn(String format, Object arg) {
    if (isLoggable(tag, WARN)) {
      Log.w(tag, format(format, arg));
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

  public void info(Throwable err, String format, Object arg) {
    if (isLoggable(tag, INFO)) {
      Log.i(tag, format(format, arg), err);
    }
  }

  public void error(Throwable e) {
    if (isLoggable(tag, ERROR)) {
      Log.e(tag, e.getMessage(), e);
    }
  }
}
