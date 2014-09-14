package l.files.operations.ui;

import android.content.Context;
import android.content.res.Resources;

import java.io.IOException;

import l.files.common.testing.BaseTest;
import l.files.operations.Clock;
import l.files.operations.Failure;
import l.files.operations.Progress;
import l.files.operations.Target;
import l.files.operations.TaskId;
import l.files.operations.TaskState;
import l.files.operations.Time;

import static android.text.format.Formatter.formatFileSize;
import static com.google.common.truth.Truth.ASSERT;
import static java.util.Arrays.asList;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.ui.Formats.formatTimeRemaining;
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

  /**
   * Returns the expected value for {@link ProgressViewer#getSmallIcon()}.
   */
  protected abstract int getSmallIcon();

  @Override protected void setUp() throws Exception {
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

  public void testGetSmallIcon() throws Exception {
    assertEquals(getSmallIcon(), viewer.getSmallIcon());
  }

  public void testGetContentTitle_Pending() throws Exception {
    ASSERT.that(viewer.getContentTitle(pending))
        .is(res.getString(R.string.pending));
  }

  public void testGetContentTitle_Failed() throws Exception {
    TaskState.Failed state = running.failed(Time.create(2, 2), asList(
        Failure.create("a", new IOException("1")),
        Failure.create("b", new IOException("2"))
    ));
    ASSERT.that(viewer.getContentTitle(state))
        .is(res.getQuantityString(getTitleFailed(), 2));
  }

  public void testGetContentTitle_Running_preparing() throws Exception {
    TaskState.Running state = setProgress(running, Progress.create(1, 0));
    state = state.running(
        Progress.create(100, 0),
        state.bytes()
    );
    ASSERT.that(viewer.getContentTitle(state)).is(res.getQuantityString(
        getTitlePreparing(), 100, 100, state.target().destination()));
  }

  public void testGetContentTitle_Running_working() throws Exception {
    TaskState.Running state = setProgress(running, Progress.create(2, 1));
    state = state.running(
        Progress.create(100, 1),
        state.bytes()
    );

    ASSERT.that(viewer.getContentTitle(state)).is(res.getQuantityString(
        getTitleRunning(), 100, 100, state.target().destination()));
  }

  public void testGetContentTitle_cleanup() throws Exception {
    TaskState.Running state = running.running(
        Progress.create(1, 1),
        Progress.create(1, 1)
    );
    ASSERT.that(viewer.getContentTitle(state))
        .is(res.getString(R.string.cleaning_up));
  }

  public void testGetProgress() throws Exception {
    TaskState.Running state = setProgress(running, Progress.create(100, 1));
    ASSERT.that(viewer.getProgress(state)).is(1 / (float) 100);
  }

  public void testGetContentText_RUNNING_showRemaining() throws Exception {
    TaskState.Running state = running.running(
        Progress.create(10, 1),
        Progress.create(20, 2)
    );
    ASSERT.that(viewer.getContentText(state))
        .is(res.getString(R.string.remain_count_x_size_x,
            10 - 1, formatFileSize(getContext(), 20 - 2)));
  }

  public void testGetContentText_noWorkDoneYet_showNothing() throws Exception {
    TaskState.Running state = running.running(
        Progress.none(),
        Progress.none()
    );
    ASSERT.that(viewer.getContentText(state)).is("");
  }

  public void testGetContentInfo_showTimeRemaining() throws Exception {
    TaskState.Running state = pending.running(Time.create(0, 0));
    state = setProgress(state, Progress.create(10000, 10));
    given(clock.tick()).willReturn(1000L);
    ASSERT.that(viewer.getContentInfo(state)).is(
        res.getString(R.string.x_countdown,
            formatTimeRemaining(0, 1000, 10000, 10).get()));
  }

  public void testGetContentInfo_noWorkDoneYet_showNothing() throws Exception {
    TaskState.Running state = pending.running(Time.create(0, 0));
    state = setProgress(state, Progress.create(1, 0));
    given(clock.tick()).willReturn(1000L);
    ASSERT.that(viewer.getContentInfo(state)).is("");
  }

}
