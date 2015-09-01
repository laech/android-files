package l.files.fs;

import com.google.auto.value.AutoValue;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Represents a point in time with nanosecond precision.
 */
@AutoValue
public abstract class Instant implements Comparable<Instant> {

  public static final Instant EPOCH = of(0, 0);

  Instant() {
  }

  /**
   * The number of seconds since epoch.
   */
  public abstract long seconds();

  /**
   * The number of nanoseconds since {@link #seconds()}.
   */
  public abstract int nanos();

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
    return new AutoValue_Instant(seconds, nanos);
  }

  /**
   * Converts this timestamp to the given unit.
   */
  public long to(TimeUnit unit) {
    long seconds = unit.convert(seconds(), SECONDS);
    long nanos = unit.convert(nanos(), NANOSECONDS);
    return seconds + nanos;
  }

  @Override public int compareTo(Instant that) {
    int initial = Long.compare(seconds(), that.seconds());
    if (initial == 0) {
      return Integer.compare(nanos(), that.nanos());
    }
    return initial;
  }

}
