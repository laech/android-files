package l.files.operations.ui.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class FailureReceiverTest extends BaseTest {

  private EventBus bus;
  private NotificationManager manager;

  @Override protected void setUp() throws Exception {
    super.setUp();
    bus = new EventBus();
    manager = mock(NotificationManager.class);
    FailureReceiver.register(bus, getContext(), manager);
  }

  public void testReceiverMethodIsAnnotated() throws Exception {
    assertNotNull(FailureReceiver.class.getMethod("on", CopyTaskInfo.class)
        .getAnnotation(Subscribe.class));
    assertNotNull(FailureReceiver.class.getMethod("on", MoveTaskInfo.class)
        .getAnnotation(Subscribe.class));
    assertNotNull(FailureReceiver.class.getMethod("on", DeleteTaskInfo.class)
        .getAnnotation(Subscribe.class));
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

    Intent intent = FailureReceiver.getFailureIntent(getContext(), task, R.plurals.fail_to_copy).get();
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

    FailureReceiver receiver = mock(FailureReceiver.class);
    doCallRealMethod().when(receiver).on(copy);
    doCallRealMethod().when(receiver).on(move);
    doCallRealMethod().when(receiver).on(delete);

    receiver.on(copy);
    receiver.on(move);
    receiver.on(delete);

    verify(receiver).on(copy, R.plurals.fail_to_copy);
    verify(receiver).on(move, R.plurals.fail_to_move);
    verify(receiver).on(delete, R.plurals.fail_to_delete);
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
