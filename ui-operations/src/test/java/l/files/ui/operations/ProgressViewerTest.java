package l.files.ui.operations;

import android.content.Context;
import android.content.res.Resources;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.operations.Clock;
import l.files.operations.Failure;
import l.files.operations.Progress;
import l.files.operations.Target;
import l.files.operations.TaskId;
import l.files.operations.TaskState;
import l.files.operations.Time;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.operations.TaskKind.COPY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Base test class for subclasses of {@link ProgressViewer}.
 */
public abstract class ProgressViewerTest {

    private Clock clock;
    private Context context;
    private Resources res;
    private ProgressViewer viewer;
    private TaskState.Pending pending;
    private TaskState.Running running;
    private RemainingTimeFormatter remainingTimeFormatter;
    private FileSizeFormatter fileSizeFormatter;

    /**
     * Creates an instance of {@link ProgressViewer} to be tested.
     */
    protected abstract ProgressViewer create(Context context, Clock clock);

    /**
     * Sets {@link TaskStateViewer#getProgress(Context, TaskState.Running)} to return the
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

    @Before
    public void setUp() throws Exception {
        res = mock(Resources.class);
        context = mock(Context.class);
        given(context.getResources()).willReturn(res);
        clock = mock(Clock.class);
        viewer = create(context, clock);
        pending = TaskState.pending(
                TaskId.create(1, COPY),
                Target.from(mock(Path.class), singleton(mock(Name.class, "src")), mock(Path.class, "dst")),
                Time.create(1, 1)
        );
        running = pending.running(
                Time.create(2, 2),
                Progress.create(1, 0),
                Progress.create(1, 0)
        );
        viewer.remainingTimeFormatter = remainingTimeFormatter = new RemainingTimeFormatter() {
            @Override
            String internalFormatTimeRemaining(long timeRemaining) {
                return String.valueOf(timeRemaining);
            }
        };
        viewer.fileSizeFormatter = fileSizeFormatter = new FileSizeFormatter() {
            @Override
            String format(Context context, long sizeBytes) {
                return String.valueOf(sizeBytes);
            }
        };
    }

    @Test
    public void getContentTitle_Pending() throws Exception {
        assertEquals(res.getString(R.string.pending),
                viewer.getContentTitle(context, pending));
    }

    @Test
    public void getContentTitle_Failed() throws Exception {
        TaskState.Failed state = running.failed(Time.create(2, 2), asList(
                Failure.create(mock(Path.class), mock(Name.class, "a"), new IOException("1")),
                Failure.create(mock(Path.class), mock(Name.class, "b"), new IOException("2"))
        ));
        String expected = res.getQuantityString(getTitleFailed(), 2);
        String actual = viewer.getContentTitle(context, state);
        assertEquals(expected, actual);
    }

    @Test
    public void getContentTitle_Running_preparing() throws Exception {
        TaskState.Running state = setProgress(running, Progress.create(1, 0));
        state = state.running(
                Progress.create(100, 0),
                state.bytes()
        );
        String expected = res.getQuantityString(
                getTitlePreparing(), 100, 100, state.target().destinationDirectory());
        String actual = viewer.getContentTitle(context, state);
        assertEquals(expected, actual);
    }

    @Test
    public void getContentTitle_Running_working() throws Exception {
        TaskState.Running state = setProgress(running, Progress.create(2, 1));
        state = state.running(
                Progress.create(100, 1),
                state.bytes()
        );

        String actual = viewer.getContentTitle(context, state);
        String expected = res.getQuantityString(
                getTitleRunning(), 100, 100, state.target().destinationDirectory());
        assertEquals(expected, actual);
    }

    @Test
    public void getContentTitle_cleanup() throws Exception {
        TaskState.Running state = running.running(
                Progress.create(1, 1),
                Progress.create(1, 1)
        );
        String actual = viewer.getContentTitle(context, state);
        String expected = res.getString(R.string.cleaning_up);
        assertEquals(expected, actual);
    }

    @Test
    public void getProgress() throws Exception {
        TaskState.Running state = setProgress(running, Progress.create(100, 1));
        float actual = viewer.getProgress(context, state);
        float expected = 1 / (float) 100;
        assertThat(actual, is(expected));
    }

    @Test
    public void getContentText_running_showRemaining() throws Exception {
        TaskState.Running state = running.running(
                Progress.create(10, 1),
                Progress.create(20, 2)
        );
        String actual = viewer.getContentText(context, state);
        String expected = res.getString(
                R.string.remain_count_x_size_x,
                10 - 1,
                fileSizeFormatter.format(context, 20 - 2)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void getContentText_noWorkDoneYet_showNothing() throws Exception {
        TaskState.Running state = running.running(Progress.NONE, Progress.NONE);
        assertEquals("", viewer.getContentText(context, state));
    }

    @Test
    public void getContentInfo_showTimeRemaining() throws Exception {
        TaskState.Running state = pending.running(Time.create(0, 0));
        state = setProgress(state, Progress.create(10000, 10));
        given(clock.tick()).willReturn(1000L);
        String actual = viewer.getContentInfo(context, state);
        String expected = res.getString(R.string.x_countdown,
                remainingTimeFormatter.format(0, 1000, 10000, 10));
        assertEquals(expected, actual);
    }

    @Test
    public void getContentInfo_noWorkDoneYet_showNothing() throws Exception {
        TaskState.Running state = pending.running(Time.create(0, 0));
        state = setProgress(state, Progress.create(1, 0));
        given(clock.tick()).willReturn(1000L);
        assertEquals("", viewer.getContentInfo(context, state));
    }

}
