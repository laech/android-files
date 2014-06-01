package l.files.operations.ui.notification;

import junit.framework.TestCase;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.operations.ui.notification.Formats.formatTimeRemaining;

public final class FormatsTest extends TestCase {

  public void testFormatTimeRemaining() {
    // total=11, processed=1, elapsed=1 sec, speed=1/sec, left=10 sec
    assertEquals("0:10", formatTimeRemaining(0, SECONDS.toMillis(1), 11, 1));

    // total=120, processed=50, elapsed=50 sec, speed=1/sec
    assertEquals("1:10", formatTimeRemaining(0, SECONDS.toMillis(50), 120, 50));

    // total=11, processed=1, elapsed=1 min, speed=1/min, left=10 min
    assertEquals("10:00", formatTimeRemaining(0, MINUTES.toMillis(1), 11, 1));

    // total=11, processed=1, elapsed=1 hr, speed=1/hr, left=10 hr
    assertEquals("10:00:00", formatTimeRemaining(0, HOURS.toMillis(1), 11, 1));
  }
}
