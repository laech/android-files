package l.files.operations.ui;

import android.content.Context;

import l.files.operations.Clock;
import l.files.operations.Progress;
import l.files.operations.TaskState;

final class DeleteViewer extends ProgressViewer {

    public DeleteViewer(final Context context, final Clock clock) {
        super(context, clock);
    }

    @Override
    protected Progress getWork(final TaskState.Running state) {
        return state.getItems();
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
    public int getSmallIcon(final Context context) {
        return R.drawable.ic_delete_white_24dp;
    }

    @Override
    protected int getTitleFailed() {
        return R.plurals.fail_to_delete;
    }
}
