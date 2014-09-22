package l.files.common.testing;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public final class Tests {
  private Tests() {}

  /**
   * Retries the given assertion by catching any {@link AssertionError}.
   * If the assertion does not succeed within the given timeout, the {@link AssertionError}
   * from the assertion will be thrown.
   */
  public static void timeout(long time, TimeUnit unit, Runnable assertion) throws InterruptedException {
    long millis = unit.toMillis(time);
    long start = currentTimeMillis();
    while (true) {
      try {
        assertion.run();
        return;
      } catch (AssertionError e) {
        if (currentTimeMillis() - start > millis) {
          throw e;
        } else {
          sleep(5);
        }
      }
    }
  }

  public static void assertExists(File file) {
    assertTrue(file + " to exist", file.exists());
  }

  public static void assertNotExists(File file) {
    assertFalse(file + " to not exist", file.exists());
  }
}
