package l.files.ui.operations;

import android.content.Context;

import l.files.operations.Clock;
import l.files.operations.Progress;
import l.files.operations.TaskState;

public final class DeleteViewerTest extends ProgressViewerTest {

    @Override
    protected DeleteViewer create(Context context, Clock clock) {
        return new DeleteViewer(context, clock);
    }

    @Override
    protected TaskState.Running setProgress(
            TaskState.Running state, Progress progress) {
        return state.running(progress, state.getBytes());
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
    protected int getTitleFailed() {
        return R.plurals.fail_to_delete;
    }

}
