package l.files.operations.ui.notification;

import com.google.common.base.Optional;

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
   * @return the formatted string, or absent if unable to determine
   */
  public static Optional<String> formatTimeRemaining(
      long startTime, long now, long total, long processed) {

    if (processed <= 0) {
      return Optional.absent();
    }

    long timeToProcessOne = (long) ((now - startTime) / (float) processed);
    long timeRemaining = (total - processed) * timeToProcessOne;
    String formatted = formatElapsedTime(MILLISECONDS.toSeconds(timeRemaining));
    if (formatted.charAt(0) == '0') {
      formatted = formatted.substring(1);
    }
    return Optional.of(formatted);
  }
}
