package l.files.ui.operations;

import android.content.Context;
import android.content.res.Resources;

import java.io.File;
import java.io.IOException;

import l.files.fs.local.LocalFile;
import l.files.operations.Clock;
import l.files.operations.Failure;
import l.files.operations.Progress;
import l.files.operations.Target;
import l.files.operations.TaskId;
import l.files.operations.TaskState;
import l.files.operations.Time;
import l.files.testing.BaseTest;

import static android.text.format.Formatter.formatFileSize;
import static java.util.Arrays.asList;
import static l.files.operations.TaskKind.COPY;
import static l.files.ui.operations.Formats.formatTimeRemaining;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Base test class for subclasses of {@link ProgressViewer}.
 */
public abstract class ProgressViewerTest extends BaseTest {

    private Clock clock;
    private Resources res;
    private ProgressViewer viewer;
    private TaskState.Pending pending;
    private TaskState.Running running;

    /**
     * Creates an instance of {@link ProgressViewer} to be tested.
     */
    protected abstract ProgressViewer create(Context context, Clock clock);

    /**
     * Sets {@link ProgressViewer#getProgress(TaskState.Running)} to return the
     * given value.
     */
    protected abstract TaskState.Running setProgress(
            TaskState.Running state, Progress progress);

    /**
     * Returns the expected value for {@link ProgressViewer#getTitlePreparing()}.
     */
    protected abstract int getTitlePreparing();

    /**
     * Returns the expected value for {@link ProgressViewer#getTitleRunning()}.
     */
    protected abstract int getTitleRunning();

    /**
     * Returns the expected value for {@link ProgressViewer#getTitleFailed()}.
     */
    protected abstract int getTitleFailed();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        res = getContext().getResources();
        clock = mock(Clock.class);
        viewer = create(getContext(), clock);
        pending = TaskState.pending(
                TaskId.create(1, COPY),
                Target.create("src", "dst"),
                Time.create(1, 1)
        );
        running = pending.running(
                Time.create(2, 2),
                Progress.create(1, 0),
                Progress.create(1, 0)
        );
    }

    public void testGetContentTitle_Pending() throws Exception {
        assertEquals(res.getString(R.string.pending),
                viewer.getContentTitle(pending));
    }

    public void testGetContentTitle_Failed() throws Exception {
        TaskState.Failed state = running.failed(Time.create(2, 2), asList(
                Failure.create(LocalFile.create(new File("a")), new IOException("1")),
                Failure.create(LocalFile.create(new File("b")), new IOException("2"))
        ));
        String expected = res.getQuantityString(getTitleFailed(), 2);
        String actual = viewer.getContentTitle(state);
        assertEquals(expected, actual);
    }

    public void testGetContentTitle_Running_preparing() throws Exception {
        TaskState.Running state = setProgress(running, Progress.create(1, 0));
        state = state.running(
                Progress.create(100, 0),
                state.getBytes()
        );
        String expected = res.getQuantityString(
                getTitlePreparing(), 100, 100, state.getTarget().destination());
        String actual = viewer.getContentTitle(state);
        assertEquals(expected, actual);
    }

    public void testGetContentTitle_Running_working() throws Exception {
        TaskState.Running state = setProgress(running, Progress.create(2, 1));
        state = state.running(
                Progress.create(100, 1),
                state.getBytes()
        );

        String actual = viewer.getContentTitle(state);
        String expected = res.getQuantityString(
                getTitleRunning(), 100, 100, state.getTarget().destination());
        assertEquals(expected, actual);
    }

    public void testGetContentTitle_cleanup() throws Exception {
        TaskState.Running state = running.running(
                Progress.create(1, 1),
                Progress.create(1, 1)
        );
        String actual = viewer.getContentTitle(state);
        String expected = res.getString(R.string.cleaning_up);
        assertEquals(expected, actual);
    }

    public void testGetProgress() throws Exception {
        TaskState.Running state = setProgress(running, Progress.create(100, 1));
        float actual = viewer.getProgress(state);
        float expected = 1 / (float) 100;
        assertEquals(expected, actual);
    }

    public void testGetContentText_RUNNING_showRemaining() throws Exception {
        TaskState.Running state = running.running(
                Progress.create(10, 1),
                Progress.create(20, 2)
        );
        String actual = viewer.getContentText(state);
        String expected = res.getString(R.string.remain_count_x_size_x,
                10 - 1, formatFileSize(getContext(), 20 - 2));
        assertEquals(expected, actual);
    }

    public void testGetContentText_noWorkDoneYet_showNothing() throws Exception {
        TaskState.Running state = running.running(Progress.NONE, Progress.NONE);
        assertEquals("", viewer.getContentText(state));
    }

    public void testGetContentInfo_showTimeRemaining() throws Exception {
        TaskState.Running state = pending.running(Time.create(0, 0));
        state = setProgress(state, Progress.create(10000, 10));
        given(clock.tick()).willReturn(1000L);
        String actual = viewer.getContentInfo(state);
        String expected = res.getString(R.string.x_countdown,
                formatTimeRemaining(0, 1000, 10000, 10));
        assertEquals(expected, actual);
    }

    public void testGetContentInfo_noWorkDoneYet_showNothing() throws Exception {
        TaskState.Running state = pending.running(Time.create(0, 0));
        state = setProgress(state, Progress.create(1, 0));
        given(clock.tick()).willReturn(1000L);
        assertEquals("", viewer.getContentInfo(state));
    }

}
