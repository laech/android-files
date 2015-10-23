package l.files.ui.operations;

import android.content.Context;

import l.files.operations.Clock;
import l.files.operations.Progress;
import l.files.operations.TaskState;

final class DeleteViewer extends ProgressViewer {

    DeleteViewer(Clock clock) {
        super(clock);
    }

    @Override
    protected Progress getWork(TaskState.Running state) {
        return state.items();
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
    public int getSmallIcon(Context context) {
        return R.drawable.ic_delete_white_24dp;
    }

    @Override
    protected int getTitleFailed() {
        return R.plurals.fail_to_delete;
    }
}
