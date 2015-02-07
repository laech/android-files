package l.files.operations

import kotlin.platform.platformStatic

data class Progress private(val total: Long, val processed: Long) {

    val processedPercentage: Float
        get() = if (NONE == this) {
            1F
        } else {
            processed.toFloat() / total.toFloat()
        }

    val left: Long get() = total - processed

    val isDone: Boolean get() = total == processed

    class object {

        val NONE = normalize(0, 0)

        /**
         * Throws IllegalArgumentException if total < processed or negative
         */
        platformStatic fun create(total: Long, processed: Long): Progress {
            if (total < 0) {
                throw IllegalArgumentException("total=" + total)
            }
            if (processed < 0) {
                throw IllegalArgumentException("processed=" + processed)
            }
            if (total < processed) {
                throw IllegalArgumentException("total=" + total + ", processed=" + processed)
            }
            return Progress(total, processed)
        }

        /**
         * If total is less than processed, set total and processed to have the
         * value of processed.
         */
        platformStatic fun normalize(total: Long, processed: Long) =
                if (total < processed) {
                    Progress(processed, processed)
                } else {
                    Progress(total, processed)
                }

    }
}
