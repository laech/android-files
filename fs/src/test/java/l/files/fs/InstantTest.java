package l.files.fs;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.Instant.EPOCH;
import static org.junit.Assert.assertEquals;

public final class InstantTest {

    @Test
    public void ofMillis_rolls_negative() throws Exception {
        assertEquals(Instant.of(-1, 0), Instant.ofMillis(-1000));
        assertEquals(Instant.of(-1, 901_000_000), Instant.ofMillis(-99));
    }

    @Test
    public void ofMillis() throws Exception {
        assertEquals(Instant.of(0, 123_000_000), Instant.ofMillis(123));
        assertEquals(Instant.of(1, 1_000_000),
                Instant.ofMillis(SECONDS.toMillis(1) + 1));
    }

    @Test
    public void of() throws Exception {
        Instant instant = Instant.of(1, 2);
        assertEquals(1, instant.seconds());
        assertEquals(2, instant.nanos());
    }

    @Test
    public void of_nanosEq0Ok() throws Exception {
        Instant.of(1, 0); // No exception
    }

    @Test
    public void of_nanosEq999_999_999Ok() throws Exception {
        Instant.of(1, (int) SECONDS.toNanos(1) - 1); // OK
    }

    @Test(expected = IllegalArgumentException.class)
    public void of_IllegalArgumentException_nanosLessThan0()
            throws Exception {

        Instant.of(1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void of_IllegalArgumentException_nanosGreaterThan999_999_999()
            throws Exception {

        Instant.of(1, (int) SECONDS.toNanos(1));
    }

    @Test
    public void to_specific_unit() throws Exception {
        Instant instant = Instant.of(98, 76543210);
        assertEquals(0L, instant.to(HOURS));
        assertEquals(1L, instant.to(MINUTES));
        assertEquals(98L, instant.to(SECONDS));
        assertEquals(98076L, instant.to(MILLISECONDS));
        assertEquals(98076543210L, instant.to(NANOSECONDS));
    }

    @Test
    public void EPOCH_is_0() throws Exception {
        assertEquals(0, EPOCH.seconds());
        assertEquals(0, EPOCH.nanos());
    }

    @Test
    public void compares_by_natural_time() throws Exception {
        List<Instant> expected = asList(
                Instant.of(1, 2),
                Instant.of(1, 2),
                Instant.of(1, 3),
                Instant.of(2, 1),
                Instant.of(2, 2)
        );
        List<Instant> actual = new ArrayList<>(expected);
        Collections.shuffle(actual);
        Collections.sort(actual);
        assertEquals(expected, actual);
    }

}
