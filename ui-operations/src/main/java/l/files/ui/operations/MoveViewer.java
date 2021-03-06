package l.files.ui.operations;

import android.content.Context;

import l.files.operations.Clock;
import l.files.operations.Progress;
import l.files.operations.TaskState;

import static l.files.ui.operations.Styles.getResourceId;

final class MoveViewer extends ProgressViewer {

    MoveViewer(Clock system) {
        super(system);
    }

    @Override
    protected Progress getWork(TaskState.Running state) {
        return state.bytes();
    }

    @Override
    protected int getTitlePreparing() {
        return R.plurals.preparing_to_move_x_items;
    }

    @Override
    protected int getTitleRunning() {
        return R.plurals.moving_x_items_to_x;
    }

    @Override
    protected int getTitleFailed() {
        return R.plurals.fail_to_move;
    }

    @Override
    public int getSmallIcon(Context context) {
        return getResourceId(android.R.attr.actionModeCutDrawable, context);
    }

}
