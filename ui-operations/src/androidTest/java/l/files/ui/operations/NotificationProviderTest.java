package l.files.ui.operations;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import l.files.fs.File;
import l.files.fs.local.LocalFile;
import l.files.operations.Clock;
import l.files.operations.Failure;
import l.files.operations.OperationService;
import l.files.operations.OperationService.TaskListener;
import l.files.operations.Target;
import l.files.operations.TaskId;
import l.files.operations.TaskNotFound;
import l.files.operations.TaskState;
import l.files.operations.Time;
import l.files.testing.BaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static l.files.operations.TaskKind.COPY;
import static l.files.ui.operations.FailuresActivity.getFailures;
import static l.files.ui.operations.FailuresActivity.getTitle;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public final class NotificationProviderTest extends BaseTest {

    private TaskState.Pending base;
    private TaskListener listener;
    private NotificationManager manager;
    private NotificationProvider provider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        base = TaskState.pending(
                TaskId.create(1, COPY), Target.NONE, Time.create(0, 0));
        manager = mock(NotificationManager.class);
        listener = mock(TaskListener.class);
        provider = new NotificationProvider(getContext(), Clock.system(), manager);
        OperationService.addListener(listener);
    }

    @Override
    protected void tearDown() throws Exception {
        OperationService.removeListener(listener);
        super.tearDown();
    }

    public void testCancelTaskNotFound() throws Exception {
        provider.onNotFound(TaskNotFound.create(1011));
        verify(manager).cancel(1011);
    }

    public void testCancelNotificationOnSuccess() {
        provider.onUpdate(base.running(Time.create(1, 1)).success(Time.create(2, 2)));
        verify(manager, timeout(1000)).cancel(base.getTask().getId());
        verifyNoMoreInteractions(manager);
    }

    public void testNotifyOnProgress() {
        provider.onUpdate(base.running(Time.create(1, 1)));
        verify(manager, timeout(1000))
                .notify(eq(base.getTask().getId()), notNull(Notification.class));
        verifyNoMoreInteractions(manager);
    }

    public void testNotifyOnFailure() throws Exception {
        File file = LocalFile.create(new java.io.File("p"));
        IOException err = new IOException("test");
        List<Failure> failures = singletonList(Failure.create(file, err));
        provider.onUpdate(base
                .running(Time.create(1, 1))
                .failed(Time.create(2, 2), failures));
        // See comment in class under test for this ID
        int id = Integer.MAX_VALUE - base.getTask().getId();
        verify(manager, timeout(1000)).notify(eq(id), notNull(Notification.class));
    }

    public void testRemoveNotificationOnUnknownError() throws Exception {
        provider.onUpdate(base
                .running(Time.create(1, 1))
                .failed(Time.create(2, 2), Collections.<Failure>emptyList()));
        verify(manager, timeout(1000)).cancel(base.getTask().getId());
    }

    public void testCreateFailureIntentWithCorrectFailureData() throws Exception {
        Intent intent = provider.getFailureIntent(base
                .running(Time.create(1, 1))
                .failed(Time.create(2, 2), asList(
                        Failure.create(LocalFile.create(new java.io.File("1")), new IOException("test1")),
                        Failure.create(LocalFile.create(new java.io.File("2")), new IOException("test2")))));
        Collection<FailureMessage> actual = getFailures(intent);
        Collection<FailureMessage> expected = asList(
                FailureMessage.create(LocalFile.create(new java.io.File("1")), "test1"),
                FailureMessage.create(LocalFile.create(new java.io.File("2")), "test2")
        );
        assertEquals(expected, actual);
        assertTrue(getTitle(intent).matches(".+"));
    }

}
