package l.files.operations.ui.notification;

import android.content.Context;

import l.files.operations.info.DeleteTaskInfo;
import l.files.operations.ui.R;

import static org.mockito.BDDMockito.given;

public final class DeleteViewerTest extends ProgressViewerTest<DeleteTaskInfo> {

    @Override
    protected DeleteViewer create(Context context, Clock clock) {
        return new DeleteViewer(context, clock);
    }

    @Override
    protected void mockWorkTotal(DeleteTaskInfo mock, int value) {
        given(mock.getTotalItemCount()).willReturn(value);
    }

    @Override
    protected void mockWorkDone(DeleteTaskInfo mock, int value) {
        given(mock.getProcessedItemCount()).willReturn(value);
    }

    @Override
    protected void mockTargetName(DeleteTaskInfo mock, String value) {
        given(mock.getSourceDirName()).willReturn(value);
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
    protected int getSmallIcon() {
        return R.drawable.ic_stat_notify_delete;
    }
}
