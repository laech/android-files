package l.files.operations.ui;

import android.content.Context;

import com.google.common.base.Optional;
import com.google.common.primitives.Ints;

import l.files.operations.Clock;
import l.files.operations.Progress;
import l.files.operations.TaskState;

import static android.text.format.Formatter.formatFileSize;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.operations.ui.Formats.formatTimeRemaining;

/**
 * Base viewer for decorating subclasses of {@link TaskState}
 * to ensure notifications are displayed in a consistent way.
 */
abstract class ProgressViewer implements TaskStateViewer {

  private final Context context;
  private final Clock clock;

  ProgressViewer(Context context, Clock clock) {
    this.context = checkNotNull(context, "context");
    this.clock = checkNotNull(clock, "clock");
  }

  @Override public final String getContentTitle(TaskState.Pending state) {
    return context.getString(R.string.pending);
  }

  @Override public final String getContentTitle(TaskState.Running state) {
    if (state.items().isDone() || state.bytes().isDone()) {
      return context.getString(R.string.cleaning_up);
    }
    int total = Ints.saturatedCast(state.items().total());
    int template = getWork(state).processed() > 0
        ? getTitleRunning() : getTitlePreparing();
    return context.getResources().getQuantityString(template, total, total,
        state.target().destination());
  }

  @Override public String getContentTitle(TaskState.Failed state) {
    return context.getResources()
        .getQuantityString(getTitleFailed(), state.failures().size());
  }

  @Override public final String getContentText(TaskState.Running state) {
    return getItemsRemaining(state);
  }

  private String getItemsRemaining(TaskState.Running state) {
    long count = state.items().left();
    long size = state.bytes().left();
    if (count == 0 || size == 0) {
      return "";
    }
    return context.getString(R.string.remain_count_x_size_x, count,
        formatFileSize(context, size));
  }

  @Override public final String getContentInfo(TaskState.Running state) {
    return getTimeRemaining(state);
  }

  private String getTimeRemaining(TaskState.Running state) {
    Optional<String> formatted = formatTimeRemaining(
        state.time().tick(),
        clock.tick(),
        getWork(state).total(),
        getWork(state).processed());
    if (formatted.isPresent()) {
      return context.getString(R.string.x_countdown, formatted.get());
    }
    return "";
  }

  @Override public final float getProgress(TaskState.Running value) {
    return getWork(value).processedPercentage();
  }

  protected abstract Progress getWork(TaskState.Running state);

  /**
   * Same template requirement as {@link #getTitleRunning()} but for the
   * preparing state.
   */
  protected abstract int getTitlePreparing();

  /**
   * Gets the title template to display when the task is in the running state.
   * The returned template must be a {@link R.plurals} template and have two
   * place holders, the first one is for the number of items being processed,
   * the other one is for {@link TaskState#target()}'s destination.
   */
  protected abstract int getTitleRunning();

  /**
   * Gets the plural title resource ID to use when the task fails.
   */
  protected abstract int getTitleFailed();
}