package l.files.fs;

import java.util.concurrent.TimeUnit;

import auto.parcel.AutoParcel;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Represents a point in time with nanosecond precision.
 */
@AutoParcel
public abstract class Instant implements Comparable<Instant> {

    public static final Instant EPOCH = of(0, 0);

    Instant() {
    }

    /**
     * The number of seconds since epoch.
     */
    public abstract long getSeconds();

    /**
     * The number of nanoseconds since {@link #getSeconds()}.
     */
    public abstract int getNanos();

    /**
     * @param seconds number of seconds since epoch
     * @param nanos   number of nanoseconds since {@code seconds}
     * @throws IllegalArgumentException if {@code nanos} is not between 0 and
     *                                  999,999,999
     */
    public static Instant of(long seconds, int nanos) {
        if (nanos < 0) {
            throw new IllegalArgumentException("nanos must be positive: " + nanos);
        }
        if (nanos > 999_999_999) {
            throw new IllegalArgumentException("nanos must be <= 999,999,999: " + nanos);
        }
        return new AutoParcel_Instant(seconds, nanos);
    }

    /**
     * Converts this timestamp to the given unit.
     */
    public long to(TimeUnit unit) {
        long seconds = unit.convert(getSeconds(), SECONDS);
        long nanos = unit.convert(getNanos(), NANOSECONDS);
        return seconds + nanos;
    }

    @Override
    public int compareTo(Instant that) {
        int initial = Long.compare(getSeconds(), that.getSeconds());
        if (initial == 0) {
            return Integer.compare(getNanos(), that.getNanos());
        }
        return initial;
    }

}
