package l.files.operations.ui.notification;

import android.os.SystemClock;

interface Clock {

    public static final Clock SYSTEM = new Clock() {
        @Override
        public long getElapsedRealTime() {
            return SystemClock.elapsedRealtime();
        }
    };

    /**
     * Returns milliseconds since boot, including time spent in sleep.
     */
    long getElapsedRealTime();
}
