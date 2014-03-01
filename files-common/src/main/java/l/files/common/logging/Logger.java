package l.files.common.logging;

import android.util.Log;

import static android.util.Log.DEBUG;
import static android.util.Log.WARN;
import static android.util.Log.isLoggable;
import static java.lang.String.format;

public final class Logger {

  private static volatile String debugTagPrefix = "";

  private final String tag;

  private Logger(String tag) {
    this.tag = tag;
  }

  public static Logger get(Class<?> target) {
    return new Logger(target.getSimpleName());
  }

  /**
   * Set a prefix for all the debug message tags, intended for testing. Don't
   * forget to undo this call with {@link #resetDebugTagPrefix()} when no longer
   * needed.
   */
  public static void setDebugTagPrefix(String prefix) {
    debugTagPrefix = prefix == null ? "" : prefix + " ";
  }

  /**
   * Clears any debug tag prefix.
   *
   * @see #setDebugTagPrefix(String)
   */
  public static void resetDebugTagPrefix() {
    setDebugTagPrefix(null);
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
    if (isLoggable(tag, DEBUG)) {
      Log.d(debugTagPrefix + tag, format(format, arg1, arg2, arg3));
    }
  }

  public void warn(Throwable err) {
    if (isLoggable(tag, WARN)) {
      Log.w(tag, err);
    }
  }
}