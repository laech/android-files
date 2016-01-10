package l.files.fs;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Represents a point in time with nanosecond precision.
 */
public final class Instant implements Comparable<Instant> {

    public static final Instant EPOCH = of(0, 0);

    private final long seconds;
    private final int nanos;

    private Instant(long seconds, int nanos) {
        this.seconds = seconds;
        this.nanos = nanos;
    }

    /**
     * The number of seconds since epoch.
     */
    public long seconds() {
        return seconds;
    }

    /**
     * The number of nanoseconds since {@link #seconds()}.
     */
    public int nanos() {
        return nanos;
    }

    public static Instant ofMillis(long time) {
        long seconds = MILLISECONDS.toSeconds(time);
        int nanos = (int) MILLISECONDS.toNanos(time - SECONDS.toMillis(seconds));
        if (nanos < 0) {
            int delta = (int) (SECONDS.toNanos(1) + nanos);
            nanos = delta;
            seconds -= (delta / (double) SECONDS.toNanos(1) + 0.5);
        }
        return of(seconds, nanos);
    }

    /**
     * @param seconds number of seconds since epoch
     * @param nanos   number of nanoseconds since {@code seconds}
     * @throws IllegalArgumentException if {@code nanos} is not between 0 and 999,999,999
     */
    public static Instant of(long seconds, int nanos) {
        if (nanos < 0) {
            throw new IllegalArgumentException(
                    "nanos must be positive" +
                            ", seconds=" + seconds +
                            ", nanos=" + nanos);
        }
        if (nanos > 999_999_999) {
            throw new IllegalArgumentException(
                    "nanos must be <= 999,999,999" +
                            ", seconds=" + seconds +
                            ", nanos=" + nanos);
        }
        return new Instant(seconds, nanos);
    }

    /**
     * Converts this timestamp to the given unit.
     */
    public long to(TimeUnit unit) {
        long seconds = unit.convert(seconds(), SECONDS);
        long nanos = unit.convert(nanos(), NANOSECONDS);
        return seconds + nanos;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Instant) {
            Instant that = (Instant) o;
            return seconds == that.seconds &&
                    nanos == that.nanos;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (seconds ^ (seconds >>> 32));
        result = 31 * result + nanos;
        return result;
    }

    @Override
    public String toString() {
        return "Instant{" +
                "seconds=" + seconds +
                ", nanos=" + nanos +
                '}';
    }

    @Override
    public int compareTo(Instant that) {
        int initial = compare(seconds(), that.seconds());
        if (initial == 0) {
            return compare(nanos(), that.nanos());
        }
        return initial;
    }

    private int compare(long a, long b) {
        return a < b ? -1 : (a == b ? 0 : 1);
    }

    private int compare(int a, int b) {
        return a < b ? -1 : (a == b ? 0 : 1);
    }

}
