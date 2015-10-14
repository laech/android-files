package l.files.ui.operations;

import android.content.Context;

import l.files.operations.Clock;
import l.files.operations.Progress;
import l.files.operations.TaskState;

import static l.files.ui.operations.Styles.getResourceId;

final class CopyViewer extends ProgressViewer {

    public CopyViewer(final Context context, final Clock clock) {
        super(context, clock);
    }

    @Override
    protected Progress getWork(final TaskState.Running state) {
        return state.getBytes();
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
    protected int getTitleFailed() {
        return R.plurals.fail_to_copy;
    }

    @Override
    public int getSmallIcon(final Context context) {
        return getResourceId(android.R.attr.actionModeCopyDrawable, context);
    }
}
