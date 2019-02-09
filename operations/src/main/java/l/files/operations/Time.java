package l.files.operations;

import androidx.annotation.Nullable;

public final class Time {

    private final long time;
    private final long tick;

    private Time(long time, long tick) {
        this.time = time;
        this.tick = tick;
    }

    /**
     * The clock time in millis, the clock changes with the user setting the
     * system clock.
     */
    public long time() {
        return time;
    }

    /**
     * Some arbitrary millis since system boot, won't be affected by user
     * changing the system time.
     */
    public long tick() {
        return tick;
    }

    public static Time create(long time, long tick) {
        return new Time(time, tick);
    }

    public static Time from(Clock clock) {
        return create(clock.time(), clock.tick());
    }

    @Override
    public String toString() {
        return "Time{" +
                "time=" + time +
                ", tick=" + tick +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Time that = (Time) o;

        return time == that.time && tick == that.tick;

    }

    @Override
    public int hashCode() {
        int result = (int) (time ^ (time >>> 32));
        result = 31 * result + (int) (tick ^ (tick >>> 32));
        return result;
    }
}
