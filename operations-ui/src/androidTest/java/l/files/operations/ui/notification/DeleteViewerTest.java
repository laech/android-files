package l.files.operations.ui.notification;

import android.content.res.Resources;

import l.files.common.testing.BaseTest;
import l.files.operations.info.DeleteTaskInfo;
import l.files.operations.ui.R;

import static l.files.operations.info.TaskInfo.TaskStatus.PENDING;
import static l.files.operations.info.TaskInfo.TaskStatus.RUNNING;
import static l.files.operations.ui.notification.Formats.formatTimeRemaining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class DeleteViewerTest extends BaseTest {

    private Clock clock;
    private Resources res;
    private DeleteTaskInfo info;

    private DeleteViewer viewer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        res = getContext().getResources();
        info = mock(DeleteTaskInfo.class);
        clock = mock(Clock.class);
        viewer = new DeleteViewer(res, clock);
    }

    public void testGetSmallIcon() throws Exception {
        assertThat(viewer.getSmallIcon(), is(R.drawable.ic_stat_notify_delete));
    }

    public void testGetContentTitle_PENDING() throws Exception {
        given(info.getTaskStatus()).willReturn(PENDING);
        assertThat(viewer.getContentTitle(info),
                is(res.getString(R.string.pending)));
    }

    public void testGetContentTitle_RUNNING_preparing() throws Exception {
        given(info.getTaskStatus()).willReturn(RUNNING);
        given(info.getDeletedItemCount()).willReturn(0);
        given(info.getTotalItemCount()).willReturn(100);
        assertThat(viewer.getContentTitle(info),
                is(res.getQuantityString(R.plurals.preparing_delete_x_items, 100, 100)));
    }

    public void testGetContentTitle_RUNNING_deleting() throws Exception {
        given(info.getTaskStatus()).willReturn(RUNNING);
        given(info.getDeletedItemCount()).willReturn(1);
        given(info.getTotalItemCount()).willReturn(100);
        assertThat(viewer.getContentTitle(info),
                is(res.getQuantityString(R.plurals.deleting_x_items, 100 - 1, 100 - 1)));
    }

    public void testGetProgress() throws Exception {
        given(info.getDeletedItemCount()).willReturn(1);
        given(info.getTotalItemCount()).willReturn(100);
        assertThat(viewer.getProgress(info), is(1 / (float) 100));
    }

    public void testGetContentText_showDeletingFrom() throws Exception {
        given(info.getSourceRootPath()).willReturn("hello");
        assertThat(viewer.getContentText(info),
                is(res.getString(R.string.from_x, "hello")));
    }

    public void testGetContentInfo_showTimeRemaining() throws Exception {
        given(info.getTotalItemCount()).willReturn(100);
        given(info.getDeletedItemCount()).willReturn(1);
        given(info.getTaskElapsedStartTime()).willReturn(0L);
        given(clock.getElapsedRealTime()).willReturn(1000L);
        assertThat(viewer.getContentInfo(info),
                is(res.getString(R.string.x_countdown,
                        formatTimeRemaining(0, 1000, 100, 1).get()))
        );
    }
}
