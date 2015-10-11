package l.files.operations.ui;

import android.content.Context;

import l.files.operations.Clock;
import l.files.operations.Progress;
import l.files.operations.TaskState;

import static android.text.format.Formatter.formatFileSize;
import static java.util.Objects.requireNonNull;
import static l.files.operations.ui.Formats.formatTimeRemaining;

/**
 * Base viewer for decorating subclasses of {@link TaskState} to ensure
 * notifications are displayed in a consistent way.
 */
abstract class ProgressViewer implements TaskStateViewer {

    private final Context context;
    private final Clock clock;

    ProgressViewer(Context context, Clock clock) {
        this.context = requireNonNull(context, "context");
        this.clock = requireNonNull(clock, "clock");
    }

    @Override
    public final String getContentTitle(TaskState.Pending state) {
        return context.getString(R.string.pending);
    }

    @Override
    public final String getContentTitle(TaskState.Running state) {
        if (state.getItems().isDone() || state.getBytes().isDone()) {
            return context.getString(R.string.cleaning_up);
        }
        int total = (int) state.getItems().getTotal();
        int template = getWork(state).getProcessed() > 0
                ? getTitleRunning() : getTitlePreparing();
        return context.getResources().getQuantityString(template, total, total,
                state.getTarget().destination());
    }

    @Override
    public String getContentTitle(TaskState.Failed state) {
        return context.getResources()
                .getQuantityString(getTitleFailed(), state.getFailures().size());
    }

    @Override
    public final String getContentText(TaskState.Running state) {
        return getItemsRemaining(state);
    }

    private String getItemsRemaining(TaskState.Running state) {
        long count = state.getItems().getLeft();
        long size = state.getBytes().getLeft();
        if (count == 0 || size == 0) {
            return "";
        }
        return context.getString(R.string.remain_count_x_size_x, count,
                formatFileSize(context, size));
    }

    @Override
    public final String getContentInfo(TaskState.Running state) {
        return getTimeRemaining(state);
    }

    private String getTimeRemaining(TaskState.Running state) {
        String formatted = formatTimeRemaining(
                state.getTime().getTick(),
                clock.tick(),
                getWork(state).getTotal(),
                getWork(state).getProcessed());
        if (formatted != null) {
            return context.getString(R.string.x_countdown, formatted);
        }
        return "";
    }

    @Override
    public final float getProgress(TaskState.Running value) {
        return getWork(value).getProcessedPercentage();
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
     * the other one is for {@link TaskState#getTarget()}'s destination.
     */
    protected abstract int getTitleRunning();

    /**
     * Gets the plural title resource ID to use when the task fails.
     */
    protected abstract int getTitleFailed();
}