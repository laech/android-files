package l.files.operations.ui;

import android.content.Context;
import android.content.res.Resources;

import java.lang.reflect.ParameterizedType;

import l.files.common.testing.BaseTest;
import l.files.operations.ProgressInfo;

import static android.text.format.Formatter.formatFileSize;
import static l.files.operations.TaskInfo.TaskStatus.PENDING;
import static l.files.operations.TaskInfo.TaskStatus.RUNNING;
import static l.files.operations.ui.Formats.formatTimeRemaining;
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

  @Override protected void setUp() throws Exception {
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
    assertEquals(getSmallIcon(), viewer.getSmallIcon());
  }

  public void testGetContentTitle_PENDING() throws Exception {
    given(info.getTaskStatus()).willReturn(PENDING);
    assertEquals(res.getString(R.string.pending), viewer.getContentTitle(info).get());
  }

  public void testGetContentTitle_RUNNING_preparing() throws Exception {
    given(info.getTaskStatus()).willReturn(RUNNING);
    given(info.getTotalItemCount()).willReturn(1);
    mockWorkDone(info, 0);
    mockTargetName(info, "hello");
    assertEquals(res.getQuantityString(getTitlePreparing(), 1, 1, "hello"),
        viewer.getContentTitle(info).get());
  }

  public void testGetContentTitle_RUNNING_copying() throws Exception {
    given(info.getTaskStatus()).willReturn(RUNNING);
    given(info.getTotalItemCount()).willReturn(100);
    mockWorkDone(info, 1);
    mockTargetName(info, "hello");
    assertEquals(res.getQuantityString(getTitleRunning(), 100, 100, "hello"),
        viewer.getContentTitle(info).get());
  }

  public void testGetContentTitle_cleanup() throws Exception {
    given(info.isCleanup()).willReturn(true);
    assertEquals(res.getString(R.string.cleaning_up), viewer.getContentTitle(info).get());
  }

  public void testGetProgress() throws Exception {
    mockWorkTotal(info, 100);
    mockWorkDone(info, 1);
    assertEquals(1 / (float) 100, viewer.getProgress(info));
  }

  public void testGetProgress_cleanup() throws Exception {
    given(info.isCleanup()).willReturn(true);
    mockWorkTotal(info, 100);
    mockWorkDone(info, 1);
    assertEquals(-1F, viewer.getProgress(info));
  }

  public void testGetContentText_PENDING_showNothing() throws Exception {
    given(info.getTaskStatus()).willReturn(PENDING);
    assertFalse(viewer.getContentText(info).isPresent());
  }

  public void testGetContentText_RUNNING_showRemaining() throws Exception {
    given(info.getTaskStatus()).willReturn(RUNNING);
    given(info.getProcessedByteCount()).willReturn(2L);
    given(info.getTotalByteCount()).willReturn(20L);
    given(info.getProcessedItemCount()).willReturn(1);
    given(info.getTotalItemCount()).willReturn(10);
    assertEquals(res.getString(
            R.string.remain_count_x_size_x, 10 - 1, formatFileSize(getContext(), 20 - 2)),
        viewer.getContentText(info).get());
  }

  public void testGetContentText_noWorkDoneYet_showNothing() throws Exception {
    given(info.getTaskStatus()).willReturn(RUNNING);
    given(info.getTotalByteCount()).willReturn(0L);
    given(info.getTotalItemCount()).willReturn(0);
    given(info.getProcessedByteCount()).willReturn(0L);
    given(info.getProcessedItemCount()).willReturn(0);
    assertFalse(viewer.getContentText(info).isPresent());
  }

  public void testGetContentText_cleanUp_showNothing() throws Exception {
    given(info.isCleanup()).willReturn(true);
    given(info.getTaskStatus()).willReturn(RUNNING);
    given(info.getTotalByteCount()).willReturn(20L);
    given(info.getTotalItemCount()).willReturn(10);
    given(info.getProcessedItemCount()).willReturn(1);
    given(info.getProcessedByteCount()).willReturn(2L);
    assertFalse(viewer.getContentText(info).isPresent());
  }

  public void testGetContentInfo_showTimeRemaining() throws Exception {
    given(info.getElapsedRealtimeOnRun()).willReturn(0L);
    given(clock.getElapsedRealTime()).willReturn(1000L);
    mockWorkTotal(info, 10000);
    mockWorkDone(info, 10);
    assertEquals(res.getString(R.string.x_countdown,
            formatTimeRemaining(0, 1000, 10000, 10).get()),
        viewer.getContentInfo(info).get());
  }

  public void testGetContentInfo_noWorkDoneYet_showNothing() throws Exception {
    given(info.getElapsedRealtimeOnRun()).willReturn(0L);
    given(clock.getElapsedRealTime()).willReturn(1000L);
    mockWorkTotal(info, 0);
    mockWorkDone(info, 0);
    assertFalse(viewer.getContentInfo(info).isPresent());
  }

  public void testGetContentInfo_cleanUp_showEmpty() throws Exception {
    given(info.isCleanup()).willReturn(true);
    given(info.getElapsedRealtimeOnRun()).willReturn(0L);
    given(clock.getElapsedRealTime()).willReturn(1000L);
    mockWorkTotal(info, 10000);
    mockWorkDone(info, 10);
    assertFalse(viewer.getContentInfo(info).isPresent());
  }
}
