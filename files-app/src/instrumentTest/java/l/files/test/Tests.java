package l.files.test;

import static android.os.SystemClock.sleep;
import static java.lang.System.currentTimeMillis;

import java.util.concurrent.TimeUnit;

public final class Tests {
  private Tests() {}

  public static void waitUntilSuccessful(Runnable code, long time, TimeUnit unit) {
    long start = currentTimeMillis();
    long duration = unit.toMillis(time);
    Throwable err = null;
    while ((start + duration) > currentTimeMillis()) {
      try {
        code.run();
      } catch (Throwable e) {
        err = e;
        sleep(5);
        continue;
      }
      return;
    }
    if (err != null) {
      throw new RuntimeException(err);
    }
  }
}
