package l.files.operations

import kotlin.platform.platformStatic

data class Time(

        /**
         * The clock time in millis, the clock changes with the user setting
         * the system clock.
         */
        val time: Long,

        /**
         * Some arbitrary millis since system boot, won't be affected by user
         * changing the system time.
         */
        val tick: Long) {

    class object {

        platformStatic fun from(clock: Clock): Time {
            return Time(clock.time(), clock.tick())
        }

    }

}
