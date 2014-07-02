package l.files.operations.ui.notification;

import android.content.Context;
import android.content.res.Resources;

import java.lang.reflect.ParameterizedType;

import l.files.common.testing.BaseTest;
import l.files.operations.info.ProgressInfo;
import l.files.operations.ui.R;

import static android.text.format.Formatter.formatFileSize;
import static l.files.operations.info.TaskInfo.TaskStatus.PENDING;
import static l.files.operations.info.TaskInfo.TaskStatus.RUNNING;
import static l.files.operations.ui.notification.Formats.formatTimeRemaining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Base test class for subclasses of {@link ProgressViewer}.
 */
public abstract class ProgressViewerTest<T extends ProgressInfo> extends BaseTest {

    private T info;
    private Clock clock;
    private Resources res;
    private ProgressViewer<T> viewer;

    /**
     * Creates an instance of {@link ProgressViewer} to be tested.
     */
    protected abstract ProgressViewer<T> create(Context context, Clock clock);

    /**
     * Mocks {@link ProgressViewer#getWorkTotal(ProgressInfo)} to return the given value.
     */
    protected abstract void mockWorkTotal(T mock, int value);

    /**
     * Mocks {@link ProgressViewer#getWorkDone(ProgressInfo)} to return the given value.
     */
    protected abstract void mockWorkDone(T mock, int value);

    /**
     * Mocks {@link ProgressViewer#getTargetName(ProgressInfo)} to return the given value.
     */
    protected abstract void mockTargetName(T mock, String value);

    /**
     * Returns the expected value for {@link ProgressViewer#getTitlePreparing()}.
     */
    protected abstract int getTitlePreparing();

    /**
     * Returns the expected value for {@link ProgressViewer#getTitleRunning()}.
     */
    protected abstract int getTitleRunning();

    /**
     * Returns the expected value for {@link ProgressViewer#getSmallIcon()}.
     */
    protected abstract int getSmallIcon();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        res = getContext().getResources();
        info = mock(getInfoClass());
        clock = mock(Clock.class);
        viewer = create(getContext(), clock);
    }

    @SuppressWarnings("unchecked")
    private Class<T> getInfoClass() {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) superclass.getActualTypeArguments()[0];
    }

    public void testGetSmallIcon() throws Exception {
        assertThat(viewer.getSmallIcon(), is(getSmallIcon()));
    }

    public void testGetContentTitle_PENDING() throws Exception {
        given(info.getTaskStatus()).willReturn(PENDING);
        assertThat(viewer.getContentTitle(info),
                is(res.getString(R.string.pending)));
    }

    public void testGetContentTitle_RUNNING_preparing() throws Exception {
        given(info.getTaskStatus()).willReturn(RUNNING);
        given(info.getTotalItemCount()).willReturn(1);
        mockWorkDone(info, 0);
        mockTargetName(info, "hello");
        assertThat(viewer.getContentTitle(info),
                is(res.getQuantityString(getTitlePreparing(), 1, 1, "hello")));
    }

    public void testGetContentTitle_RUNNING_copying() throws Exception {
        given(info.getTaskStatus()).willReturn(RUNNING);
        given(info.getTotalItemCount()).willReturn(100);
        mockWorkDone(info, 1);
        mockTargetName(info, "hello");
        assertThat(viewer.getContentTitle(info),
                is(res.getQuantityString(getTitleRunning(), 100, 100, "hello")));
    }

    public void testGetProgress() throws Exception {
        mockWorkTotal(info, 100);
        mockWorkDone(info, 1);
        assertThat(viewer.getProgress(info), is(1 / (float) 100));
    }

    public void testGetContentText_PENDING_showNothing() throws Exception {
        given(info.getTaskStatus()).willReturn(PENDING);
        assertThat(viewer.getContentText(info), is(nullValue()));
    }

    public void testGetContentText_RUNNING_showRemaining() throws Exception {
        given(info.getTaskStatus()).willReturn(RUNNING);
        given(info.getProcessedByteCount()).willReturn(2L);
        given(info.getTotalByteCount()).willReturn(20L);
        given(info.getProcessedItemCount()).willReturn(1);
        given(info.getTotalItemCount()).willReturn(10);
        assertThat(viewer.getContentText(info), is(res.getString(R.string.remain_count_x_size_x,
                        10 - 1, formatFileSize(getContext(), 20 - 2)))
        );
    }

    public void testGetContentInfo_showTimeRemaining() throws Exception {
        given(info.getTaskElapsedStartTime()).willReturn(0L);
        given(clock.getElapsedRealTime()).willReturn(1000L);
        mockWorkTotal(info, 10000);
        mockWorkDone(info, 10);
        assertThat(viewer.getContentInfo(info),
                is(res.getString(R.string.x_countdown,
                        formatTimeRemaining(0, 1000, 10000, 10).get()))
        );
    }
}
