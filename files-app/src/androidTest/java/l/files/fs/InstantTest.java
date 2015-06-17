package l.files.fs;

import junit.framework.TestCase;

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

public final class InstantTest extends TestCase
{
    public void test_ofMillis_rolls_negative() throws Exception
    {
        assertEquals(Instant.of(-1, 0), Instant.ofMillis(-1000));
        assertEquals(Instant.of(-1, 901_000_000), Instant.ofMillis(-99));
    }

    public void test_ofMillis() throws Exception
    {
        assertEquals(Instant.of(0, 123_000_000), Instant.ofMillis(123));
        assertEquals(Instant.of(1, 1_000_000),
                Instant.ofMillis(SECONDS.toMillis(1) + 1));
    }

    public void test_of() throws Exception
    {
        final Instant instant = Instant.of(1, 2);
        assertEquals(1, instant.seconds());
        assertEquals(2, instant.nanos());
    }

    public void test_of_nanosEq0Ok() throws Exception
    {
        Instant.of(1, 0); // No exception
    }

    public void test_of_nanosEq999_999_999Ok() throws Exception
    {
        Instant.of(1, (int) SECONDS.toNanos(1) - 1); // OK
    }

    public void test_of_IllegalArgumentException_nanosLessThan0()
            throws Exception
    {
        try
        {
            Instant.of(1, -1);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            // Pass
        }
    }

    public void test_of_IllegalArgumentException_nanosGreaterThan999_999_999()
            throws Exception
    {
        try
        {
            Instant.of(1, (int) SECONDS.toNanos(1));
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            // Pass
        }
    }

    public void test_to() throws Exception
    {
        final Instant instant = Instant.of(98, 76543210);
        assertEquals(0L, instant.to(HOURS));
        assertEquals(1L, instant.to(MINUTES));
        assertEquals(98L, instant.to(SECONDS));
        assertEquals(98076L, instant.to(MILLISECONDS));
        assertEquals(98076543210L, instant.to(NANOSECONDS));
    }

    public void test_EPOCH() throws Exception
    {
        assertEquals(0, EPOCH.seconds());
        assertEquals(0, EPOCH.nanos());
    }

    public void test_compareTo() throws Exception
    {
        final List<Instant> expected = asList(
                Instant.of(1, 2),
                Instant.of(1, 2),
                Instant.of(1, 3),
                Instant.of(2, 1),
                Instant.of(2, 2)
        );
        final List<Instant> actual = new ArrayList<>(expected);
        Collections.shuffle(actual);
        Collections.sort(actual);
        assertEquals(expected, actual);
    }

}
