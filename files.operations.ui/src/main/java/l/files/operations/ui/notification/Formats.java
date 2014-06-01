package l.files.operations.ui.notification;

import static android.text.format.DateUtils.formatElapsedTime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

final class Formats {
  private Formats() {}

  /**
   * Formats the remaining time for a task.
   * <p/>
   * e.g. "1:30"
   *
   * @param startTime start time of processing in milliseconds
   * @param now current time in milliseconds
   * @param total the total number of work
   * @param processed the number of work done
   * @throws IllegalArgumentException if {@code processed} is <= 0
   */
  public static String formatTimeRemaining(
      long startTime, long now, long total, long processed) {

    if (processed <= 0) {
      throw new IllegalArgumentException("processed=" + processed);
    }

    long timeToProcessOne = (long) ((now - startTime) / (float) processed);
    long timeRemaining = (total - processed) * timeToProcessOne;
    String formatted = formatElapsedTime(MILLISECONDS.toSeconds(timeRemaining));
    if (formatted.charAt(0) == '0') {
      formatted = formatted.substring(1);
    }
    return formatted;
  }
}
