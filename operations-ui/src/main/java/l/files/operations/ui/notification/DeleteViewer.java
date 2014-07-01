package l.files.operations.ui.notification;

import android.content.Context;
import android.content.res.Resources;

import com.google.common.base.Optional;

import l.files.operations.info.DeleteTaskInfo;
import l.files.operations.ui.R;

import static android.text.format.Formatter.formatFileSize;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.operations.info.TaskInfo.TaskStatus.PENDING;
import static l.files.operations.ui.R.drawable;
import static l.files.operations.ui.R.plurals.deleting_x_items_from_x;
import static l.files.operations.ui.R.plurals.preparing_delete_x_items_from_x;
import static l.files.operations.ui.R.string.pending;
import static l.files.operations.ui.R.string.remain_count_x_size_x;
import static l.files.operations.ui.notification.Formats.formatTimeRemaining;

final class DeleteViewer implements NotificationViewer<DeleteTaskInfo> {

    private final Context context;
    private final Resources res;
    private final Clock clock;

    DeleteViewer(Context context, Clock clock) {
        this.context = checkNotNull(context, "context");
        this.res = context.getResources();
        this.clock = checkNotNull(clock, "clock");
    }

    @Override
    public int getSmallIcon() {
        return drawable.ic_stat_notify_delete;
    }

    @Override
    public String getContentTitle(DeleteTaskInfo value) {
        switch (value.getTaskStatus()) {
            case PENDING:
                return getTitleForStatusPending();
            case RUNNING:
                if (value.getDeletedItemCount() == 0) {
                    return getTitleForStatusPreparing(value);
                } else {
                    return getTitleForStatusProcessing(value);
                }
            default:
                return null;
        }
    }

    private String getTitleForStatusPending() {
        return res.getString(pending);
    }

    private String getTitleForStatusPreparing(DeleteTaskInfo value) {
        int count = value.getTotalItemCount();
        return res.getQuantityString(preparing_delete_x_items_from_x, count, count, value.getDirName());
    }

    private String getTitleForStatusProcessing(DeleteTaskInfo value) {
        int count = value.getTotalItemCount();
        return res.getQuantityString(deleting_x_items_from_x, count, count, value.getDirName());
    }

    @Override
    public float getProgress(DeleteTaskInfo value) {
        return value.getDeletedItemCount() / (float) value.getTotalItemCount();
    }

    @Override
    public String getContentText(DeleteTaskInfo value) {
        if (value.getTaskStatus() == PENDING) {
            return null;
        }
        int count = value.getTotalItemCount() - value.getDeletedItemCount();
        long size = value.getTotalByteCount() - value.getDeletedByteCount();
        return res.getString(remain_count_x_size_x, count, formatFileSize(context, size));
    }

    @Override
    public String getContentInfo(DeleteTaskInfo value) {
        return getTimeRemaining(value);
    }

    private String getTimeRemaining(DeleteTaskInfo value) {
        Optional<String> formatted = formatTimeRemaining(
                value.getTaskElapsedStartTime(),
                clock.getElapsedRealTime(),
                value.getTotalItemCount(),
                value.getDeletedItemCount());
        if (formatted.isPresent()) {
            return res.getString(R.string.x_countdown, formatted.get());
        }
        return null;
    }
}
