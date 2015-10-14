package l.files.ui.operations;

import static android.text.format.DateUtils.formatElapsedTime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

final class Formats {
    private Formats() {
    }

    /**
     * Formats the remaining time for a task.
     * <p/>
     * e.g. "1:30"
     *
     * @param startTime start time of processing in milliseconds
     * @param now       current time in milliseconds
     * @param total     the total number of work
     * @param processed the number of work done
     * @return the formatted string, or null if unable to determine
     */
    public static String formatTimeRemaining(
            long startTime, long now, long total, long processed) {

        if (processed <= 0) {
            return null;
        }

        float timeToProcessOne = (now - startTime) / (float) processed;
        float timeRemaining = (total - processed) * timeToProcessOne;
        String formatted = formatElapsedTime(MILLISECONDS.toSeconds((long) timeRemaining));
        if (formatted.charAt(0) == '0' && formatted.charAt(1) != ':') {
            formatted = formatted.substring(1);
        }
        return formatted;
    }
}
