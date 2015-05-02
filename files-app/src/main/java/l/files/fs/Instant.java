package l.files.fs;

import java.util.concurrent.TimeUnit;

import auto.parcel.AutoParcel;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Represents a point in time with nanosecond precision.
 */
@AutoParcel
public abstract class Instant implements Comparable<Instant>
{
    public static final Instant EPOCH = of(0, 0);

    Instant()
    {
    }

    /**
     * The number of seconds since epoch.
     */
    public abstract long seconds();

    /**
     * The number of nanoseconds since {@link #seconds()}.
     */
    public abstract int nanos();

    /**
     * @param seconds
     *         number of seconds since epoch
     * @param nanos
     *         number of nanoseconds since {@code seconds}
     * @throws IllegalArgumentException
     *         if {@code nanos} is not between 0 and 999,999,999
     */
    public static Instant of(final long seconds, final int nanos)
    {
        if (nanos < 0)
        {
            throw new IllegalArgumentException(
                    "nanos must be positive" +
                            ", seconds=" + seconds +
                            ", nanos=" + nanos);
        }
        if (nanos > 999_999_999)
        {
            throw new IllegalArgumentException(
                    "nanos must be <= 999,999,999" +
                            ", seconds=" + seconds +
                            ", nanos=" + nanos);
        }
        return new AutoParcel_Instant(seconds, nanos);
    }

    /**
     * Converts this timestamp to the given unit.
     */
    public long to(final TimeUnit unit)
    {
        final long seconds = unit.convert(seconds(), SECONDS);
        final long nanos = unit.convert(nanos(), NANOSECONDS);
        return seconds + nanos;
    }

    @Override
    public int compareTo(final Instant that)
    {
        final int initial = Long.compare(seconds(), that.seconds());
        if (initial == 0)
        {
            return Integer.compare(nanos(), that.nanos());
        }
        return initial;
    }

}
