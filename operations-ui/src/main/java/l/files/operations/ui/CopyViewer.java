package l.files.operations.ui;

import android.content.Context;

import l.files.operations.CopyTaskInfo;

final class CopyViewer extends ProgressViewer<CopyTaskInfo> {

    CopyViewer(Context context, Clock clock) {
        super(context, clock);
    }

    @Override
    protected long getWorkTotal(CopyTaskInfo value) {
        return value.getTotalByteCount();
    }

    @Override
    protected long getWorkDone(CopyTaskInfo value) {
        return value.getProcessedByteCount();
    }

    @Override
    protected String getTargetName(CopyTaskInfo value) {
        return value.getDestinationName();
    }

    @Override
    protected int getTitlePreparing() {
        return R.plurals.preparing_to_copy_x_items_to_x;
    }

    @Override
    protected int getTitleRunning() {
        return R.plurals.copying_x_items_to_x;
    }

    @Override
    public int getSmallIcon() {
        return R.drawable.ic_stat_notify_copy;
    }

}
