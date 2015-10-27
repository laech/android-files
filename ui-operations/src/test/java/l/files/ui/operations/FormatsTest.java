package l.files.ui.operations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.ui.operations.Formats.formatTimeRemaining;
import static org.junit.Assert.assertEquals;

@RunWith(ParameterizedRobolectricTestRunner.class)
public final class FormatsTest {

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][]{

                // total=11, processed=1, elapsed=1 sec, speed=1/sec, left=10 sec
                {"0:10", 0, SECONDS.toMillis(1), 11, 1},

                // total=120, processed=50, elapsed=50 sec, speed=1/sec
                {"1:10", 0, SECONDS.toMillis(50), 120, 50},

                // total=11, processed=1, elapsed=1 min, speed=1/min, left=10 min
                {"10:00", 0, MINUTES.toMillis(1), 11, 1},

                // total=11, processed=1, elapsed=1 hr, speed=1/hr, left=10 hr
                {"10:00:00", 0, HOURS.toMillis(1), 11, 1},

                // total=2_000_000, processed=1000, elapsed=1 ms, speed=1_000_000/sec
                {"0:01", 0, 1, 2_000_000, 1000},

        });
    }

    private final String expected;
    private final long startTime;
    private final long now;
    private final long total;
    private final long processed;

    public FormatsTest(String expected, long startTime, long now, long total, long processed) {
        this.expected = expected;
        this.startTime = startTime;
        this.now = now;
        this.total = total;
        this.processed = processed;
    }

    @Test
    public void format_time_remaining() {
        assertEquals(expected, formatTimeRemaining(startTime, now, total, processed));
    }

}
