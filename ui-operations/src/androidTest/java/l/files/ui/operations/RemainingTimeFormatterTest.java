package l.files.ui.operations;

import org.junit.Test;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.ui.operations.RemainingTimeFormatter.INSTANCE;
import static org.junit.Assert.assertEquals;

public final class RemainingTimeFormatterTest {

    @Test
    public void format_time_remaining() {
        // total=11, processed=1, elapsed=1 sec, speed=1/sec, left=10 sec
        assertEquals("0:10", INSTANCE.format(0, SECONDS.toMillis(1), 11, 1));

        // total=120, processed=50, elapsed=50 sec, speed=1/sec
        assertEquals("1:10", INSTANCE.format(0, SECONDS.toMillis(50), 120, 50));

        // total=11, processed=1, elapsed=1 min, speed=1/min, left=10 min
        assertEquals("10:00", INSTANCE.format(0, MINUTES.toMillis(1), 11, 1));

        // total=11, processed=1, elapsed=1 hr, speed=1/hr, left=10 hr
        assertEquals("10:00:00", INSTANCE.format(0, HOURS.toMillis(1), 11, 1));

        // total=2_000_000, processed=1000, elapsed=1 ms, speed=1_000_000/sec
        assertEquals("0:01", INSTANCE.format(0, 1, 2_000_000, 1000));
    }

}
