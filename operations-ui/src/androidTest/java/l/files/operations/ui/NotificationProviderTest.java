package l.files.operations.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;

import java.io.IOException;
import java.util.Collection;

import de.greenrobot.event.EventBus;
import l.files.common.testing.BaseTest;
import l.files.eventbus.Events;
import l.files.operations.Clock;
import l.files.operations.Failure;
import l.files.operations.Target;
import l.files.operations.TaskId;
import l.files.operations.TaskNotFound;
import l.files.operations.TaskState;
import l.files.operations.Time;

import static com.google.common.truth.Truth.ASSERT;
import static java.util.Arrays.asList;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.ui.FailuresActivity.getFailures;
import static l.files.operations.ui.FailuresActivity.getTitle;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public final class NotificationProviderTest extends BaseTest {

  private TaskState.Pending base;
  private EventBus bus;
  private NotificationManager manager;
  private NotificationProvider provider;

  @Override protected void setUp() throws Exception {
    super.setUp();
    base = TaskState.pending(
        TaskId.create(1, COPY), Target.none(), Time.create(0, 0));
    manager = mock(NotificationManager.class);
    bus = Events.failFast(new EventBus());
    provider = new NotificationProvider(getContext(), Clock.system(), manager);
    bus.register(provider);
  }

  public void testCancelTaskNotFound() throws Exception {
    bus.post(TaskNotFound.create(1011));
    verify(manager).cancel(1011);
  }

  public void testCancelNotificationOnSuccess() {
    bus.post(base.running(Time.create(1, 1)).success(Time.create(2, 2)));
    verify(manager, timeout(1000)).cancel(base.task().id());
    verifyNoMoreInteractions(manager);
  }

  public void testNotifyOnProgress() {
    bus.post(base.running(Time.create(1, 1)));
    verify(manager, timeout(1000))
        .notify(eq(base.task().id()), notNull(Notification.class));
    verifyNoMoreInteractions(manager);
  }

  public void testNotifyOnFailure() throws Exception {
    bus.post(base.running(Time.create(1, 1)).failed(Time.create(2, 2),
        asList(Failure.create("p", new IOException("test")))));
    verify(manager, timeout(1000))
        .notify(eq(base.task().id()), notNull(Notification.class));
  }

  public void testCreateFailureIntentWithCorrectFailureData() throws Exception {
    Intent intent = provider.getFailureIntent(base
        .running(Time.create(1, 1))
        .failed(Time.create(2, 2), asList(
            Failure.create("1", new IOException("test1")),
            Failure.create("2", new IOException("test2")))));
    Collection<FailureMessage> actual = getFailures(intent);
    Collection<FailureMessage> expected = asList(
        FailureMessage.create("1", "test1"),
        FailureMessage.create("2", "test2")
    );
    ASSERT.that(actual).is(expected);
    ASSERT.that(getTitle(intent)).matches(".+");
  }

}
