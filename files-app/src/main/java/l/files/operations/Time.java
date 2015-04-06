package l.files.operations;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class Time {

    Time() {
    }

    /**
     * The clock time in millis, the clock changes with the user setting the
     * system clock.
     */
    public abstract long getTime();

    /**
     * Some arbitrary millis since system boot, won't be affected by user
     * changing the system time.
     */
    public abstract long getTick();

    public static Time create(long time, long tick) {
        return new AutoParcel_Time(time, tick);
    }

    public static Time from(Clock clock) {
        return create(clock.time(), clock.tick());
    }

}
