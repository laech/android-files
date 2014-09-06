package l.files.operations.ui.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import de.greenrobot.event.EventBus;
import l.files.common.testing.BaseTest;
import l.files.operations.info.CopyTaskInfo;
import l.files.operations.info.DeleteTaskInfo;
import l.files.operations.info.MoveTaskInfo;
import l.files.operations.info.TaskInfo;
import l.files.operations.ui.FailureMessage;
import l.files.operations.ui.R;

import static java.util.Arrays.asList;
import static l.files.io.file.operations.FileOperation.Failure;
import static l.files.operations.info.TaskInfo.TaskStatus.FINISHED;
import static l.files.operations.info.TaskInfo.TaskStatus.RUNNING;
import static l.files.operations.ui.FailuresActivity.getFailures;
import static l.files.operations.ui.FailuresActivity.getTitle;
import static l.files.operations.ui.notification.NotificationReceiver.getFailureIntent;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class NotificationReceiverTest extends BaseTest {

  private EventBus bus;
  private NotificationManager manager;

  @Override protected void setUp() throws Exception {
    super.setUp();
    manager = mock(NotificationManager.class);
    bus = new EventBus();
    bus.register(new NotificationReceiver(getContext(), manager));
  }

  public void testCopyTaskInfoReceiverMethodIsAnnotated() throws Exception {
    testReceiverMethodIsAnnotated(CopyTaskInfo.class);
  }

  public void testMoveTaskInfoReceiverMethodIsAnnotated() throws Exception {
    testReceiverMethodIsAnnotated(MoveTaskInfo.class);
  }

  public void testDeleteTaskInfoReceiverMethodIsAnnotated() throws Exception {
    testReceiverMethodIsAnnotated(DeleteTaskInfo.class);
  }

  private void testReceiverMethodIsAnnotated(Class<?> parameterType)
      throws NoSuchMethodException {
    assertNotNull(NotificationReceiver.class
        .getMethod("onEventMainThread", parameterType));
  }

  public void testCancelNotificationOnSuccess() {
    CopyTaskInfo task = mock(CopyTaskInfo.class);
    given(task.getTaskId()).willReturn(101);
    given(task.getTaskStatus()).willReturn(FINISHED);
    given(task.getFailures()).willReturn(Collections.<Failure>emptyList());

    bus.post(task);
    verify(manager).cancel(task.getTaskId());
    verifyNoMoreInteractions(manager);
  }

  public void testNotifyOnProgress() {
    CopyTaskInfo task = mock(CopyTaskInfo.class);
    given(task.getTaskId()).willReturn(101);
    given(task.getTaskStatus()).willReturn(RUNNING);
    given(task.getFailures()).willReturn(Collections.<Failure>emptyList());

    bus.post(task);
    verify(manager).notify(eq(task.getTaskId()), notNull(Notification.class));
    verifyNoMoreInteractions(manager);
  }

  public void testNotifyOnFailure() throws Exception {
    TaskInfo task = newTaskWithFailure();
    bus.post(task);
    verify(manager).notify(eq(task.getTaskId()), notNull(Notification.class));
  }

  public void testNoNotifyIfTaskIsNotFinished() throws Exception {
    TaskInfo task = newTaskWithFailure();
    given(task.getTaskStatus()).willReturn(RUNNING);
    verifyZeroInteractions(manager);
  }

  public void testNoNotifyIfTaskHasNoFailure() throws Exception {
    TaskInfo task = newTaskWithFailure();
    given(task.getFailures()).willReturn(Collections.<Failure>emptyList());
    verifyZeroInteractions(manager);
  }

  public void testCreatesFailureIntentWithCorrectFailureData() throws Exception {
    TaskInfo task = newTaskWithFailure();
    given(task.getFailures()).willReturn(asList(
        Failure.create("1", new IOException("test1")),
        Failure.create("2", new IOException("test2"))
    ));

    Intent intent = getFailureIntent(getContext(), task, R.plurals.fail_to_copy).get();
    Collection<FailureMessage> actual = getFailures(intent);
    Collection<FailureMessage> expected = asList(
        FailureMessage.create("1", "test1"),
        FailureMessage.create("2", "test2")
    );
    assertEquals(expected, actual);
    assertEquals(
        getContext().getResources().getQuantityString(R.plurals.fail_to_copy, 2),
        getTitle(intent));
  }

  public void testDelegatesCorrectTitleResourceId() {
    CopyTaskInfo copy = newTaskWithFailure(CopyTaskInfo.class);
    MoveTaskInfo move = newTaskWithFailure(MoveTaskInfo.class);
    DeleteTaskInfo delete = newTaskWithFailure(DeleteTaskInfo.class);

    NotificationReceiver receiver = mock(NotificationReceiver.class);
    doCallRealMethod().when(receiver).onEventMainThread(copy);
    doCallRealMethod().when(receiver).onEventMainThread(move);
    doCallRealMethod().when(receiver).onEventMainThread(delete);

    receiver.onEventMainThread(copy);
    receiver.onEventMainThread(move);
    receiver.onEventMainThread(delete);

    verify(receiver).onFailure(copy, R.plurals.fail_to_copy);
    verify(receiver).onFailure(move, R.plurals.fail_to_move);
    verify(receiver).onFailure(delete, R.plurals.fail_to_delete);
  }

  public TaskInfo newTaskWithFailure() {
    return newTaskWithFailure(CopyTaskInfo.class);
  }

  public <T extends TaskInfo> T newTaskWithFailure(Class<T> clazz) {
    T value = mock(clazz);
    given(value.getTaskId()).willReturn(101);
    given(value.getTaskStatus()).willReturn(FINISHED);
    given(value.getFailures()).willReturn(asList(Failure.create("p", new IOException("test"))));
    return value;
  }

}
