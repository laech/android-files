package l.files.operations.ui;

import android.content.Context;

import l.files.operations.DeleteTaskInfo;

final class DeleteViewer extends ProgressViewer<DeleteTaskInfo> {

    DeleteViewer(Context context, Clock clock) {
        super(context, clock);
    }

    @Override
    protected long getWorkTotal(DeleteTaskInfo value) {
        return value.getTotalItemCount();
    }

    @Override
    protected long getWorkDone(DeleteTaskInfo value) {
        return value.getProcessedItemCount();
    }

    @Override
    protected String getTargetName(DeleteTaskInfo value) {
        return value.getSourceDirName();
    }

    @Override
    protected int getTitlePreparing() {
        return R.plurals.preparing_delete_x_items_from_x;
    }

    @Override
    protected int getTitleRunning() {
        return R.plurals.deleting_x_items_from_x;
    }

    @Override
    public int getSmallIcon() {
        return R.drawable.ic_stat_notify_delete;
    }
}
