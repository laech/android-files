package l.files.operations.ui.notification;

import android.content.Context;

import l.files.operations.CopyTaskInfo;
import l.files.operations.ui.R;

import static org.mockito.BDDMockito.given;

public final class CopyViewerTest extends ProgressViewerTest<CopyTaskInfo> {

    @Override
    protected CopyViewer create(Context context, Clock clock) {
        return new CopyViewer(context, clock);
    }

    @Override
    protected void mockWorkTotal(CopyTaskInfo mock, int value) {
        given(mock.getTotalByteCount()).willReturn((long) value);
    }

    @Override
    protected void mockWorkDone(CopyTaskInfo mock, int value) {
        given(mock.getProcessedByteCount()).willReturn((long) value);
    }

    @Override
    protected void mockTargetName(CopyTaskInfo mock, String value) {
        given(mock.getDestinationName()).willReturn(value);
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
    protected int getSmallIcon() {
        return R.drawable.ic_stat_notify_copy;
    }
}
