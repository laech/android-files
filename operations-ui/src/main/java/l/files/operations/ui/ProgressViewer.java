package l.files.operations.ui;

import android.content.Context;

import com.google.common.base.Optional;

import l.files.operations.ProgressInfo;

import static android.text.format.Formatter.formatFileSize;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.operations.TaskInfo.TaskStatus.PENDING;
import static l.files.operations.ui.Formats.formatTimeRemaining;

/**
 * Base viewer for decorating subclasses of {@link ProgressInfo}
 * to ensure notifications are displayed in a consistent way.
 */
abstract class ProgressViewer<T extends ProgressInfo> implements NotificationViewer<T> {

  private final Context context;
  private final Clock clock;

  ProgressViewer(Context context, Clock clock) {
    this.context = checkNotNull(context, "context");
    this.clock = checkNotNull(clock, "clock");
  }

  @Override public final Optional<String> getContentTitle(T value) {
    if (value.isCleanup()) {
      return Optional.of(context.getString(R.string.cleaning_up));
    }
    switch (value.getTaskStatus()) {
      case PENDING:
        return Optional.of(context.getString(R.string.pending));
      case RUNNING:
        int template = getWorkDone(value) > 0 ? getTitleRunning() : getTitlePreparing();
        return Optional.of(context.getResources().getQuantityString(template,
            value.getTotalItemCount(),
            value.getTotalItemCount(),
            getTargetName(value)));
      default:
        return Optional.absent();
    }
  }

  @Override public final Optional<String> getContentText(T value) {
    if (value.isCleanup()) {
      return Optional.absent();
    }
    return getItemsRemaining(value);
  }

  private Optional<String> getItemsRemaining(T value) {
    if (value.getTaskStatus() == PENDING) {
      return Optional.absent();
    }
    int count = value.getTotalItemCount() - value.getProcessedItemCount();
    long size = value.getTotalByteCount() - value.getProcessedByteCount();
    if (count == 0 || size == 0) {
      return Optional.absent();
    }
    return Optional.of(context.getString(R.string.remain_count_x_size_x,
        count, formatFileSize(context, size)));
  }

  @Override public final Optional<String> getContentInfo(T value) {
    if (value.isCleanup()) {
      return Optional.absent();
    }
    return getTimeRemaining(value);
  }

  private Optional<String> getTimeRemaining(T value) {
    Optional<String> formatted = formatTimeRemaining(
        value.getElapsedRealtimeOnRun(),
        clock.getElapsedRealTime(),
        getWorkTotal(value),
        getWorkDone(value));
    if (formatted.isPresent()) {
      return Optional.of(context.getString(R.string.x_countdown, formatted.get()));
    }
    return Optional.absent();
  }

  @Override public final float getProgress(T value) {
    if (value.isCleanup()) {
      return -1;
    }
    return getWorkDone(value) / (float) getWorkTotal(value);
  }

  /**
   * Gets the number of work unit in total.
   * The returned value will be used in calculate the progress.
   */
  protected abstract long getWorkTotal(T value);

  /**
   * Gets the number of work unit done so far.
   * The returned value will be used to calculate the progress.
   */
  protected abstract long getWorkDone(T value);

  /**
   * Gets the target name to be placed into the title templates.
   *
   * @see #getTitlePreparing()
   * @see #getTitleRunning()
   */
  protected abstract String getTargetName(T value);

  /**
   * Same template requirement as {@link #getTitleRunning()} but for the preparing state.
   */
  protected abstract int getTitlePreparing();

  /**
   * Gets the title template to display when the task is in the running state.
   * The returned template must be a {@link R.plurals} template and have two place holders,
   * the first one is for the number of items being processed, the other one is for
   * {@link #getTargetName(ProgressInfo)}.
   */
  protected abstract int getTitleRunning();
}
