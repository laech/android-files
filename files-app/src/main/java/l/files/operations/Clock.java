package l.files.operations;

import static android.os.SystemClock.elapsedRealtime;
import static java.lang.System.currentTimeMillis;

public abstract class Clock {

    private static final Clock SYSTEM = new Clock() {
        @Override
        public long time() {
            return currentTimeMillis();
        }

        @Override
        public long tick() {
            return elapsedRealtime();
        }
    };

    public static Clock system() {
        return SYSTEM;
    }

    /**
     * Reads the current time.
     */
    public Time read() {
        return Time.from(this);
    }

    /**
     * The clock time in millis, the clock changes with the user setting the
     * system clock.
     */
    public abstract long time();

    /**
     * Some arbitrary millis since system boot, won't be affected by user changing
     * the system time.
     */
    public abstract long tick();
}
