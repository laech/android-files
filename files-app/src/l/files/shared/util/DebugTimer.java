package l.files.shared.util;

import static l.files.shared.BuildConfig.DEBUG;
import android.os.SystemClock;
import android.util.Log;

public final class DebugTimer {

  public static DebugTimer start(String tag) {
    DebugTimer timer = new DebugTimer(tag);
    timer.start();
    return timer;
  }

  private final String tag;
  private long start;

  private DebugTimer(String tag) {
    this.tag = tag;
  }

  public void start() {
    if (DEBUG) start = now();
  }

  private long now() {
    return SystemClock.elapsedRealtime();
  }

  public void log(Object label) {
    if (DEBUG) doLog(label);
  }

  public void log(Object label1, Object label2) {
    if (DEBUG) doLog(label1 + ", " + label2);
  }

  private void doLog(Object label) {
    Log.d(tag, label + ": " + (now() - start) + " ms");
  }
}
