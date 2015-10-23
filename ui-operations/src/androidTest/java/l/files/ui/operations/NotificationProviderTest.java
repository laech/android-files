package l.files.ui.operations;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import l.files.fs.File;
import l.files.fs.local.LocalFile;
import l.files.operations.Failure;
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
    private Context context;
    private NotificationManager manager;
    private NotificationProvider provider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = new NotificationProvider();
        base = TaskState.pending(
                TaskId.create(1, COPY),
                mock(Target.class),
                Time.create(0, 0)
        );
        manager = mock(NotificationManager.class);
        context = new ContextWrapper(getContext()) {

            @Override
            public Object getSystemService(String name) {
                if (NOTIFICATION_SERVICE.equals(name)) {
                    return manager;
                }
                return super.getSystemService(name);
            }

            @Override
            public Context getApplicationContext() {
                return this;
            }

        };
    }

    public void test_cancel_on_task_not_found() throws Exception {

        provider.onNotFound(context, TaskNotFound.create(1011));
        verify(manager).cancel(1011);
    }

    public void test_cancel_notification_on_success() {

        provider.onUpdate(context, base.running(Time.create(1, 1)).success(Time.create(2, 2)));
        verify(manager, timeout(1000)).cancel(base.task().id());
        verifyNoMoreInteractions(manager);
    }

    public void test_notify_on_progress() {

        provider.onUpdate(context, base.running(Time.create(1, 1)));
        verify(manager, timeout(1000))
                .notify(eq(base.task().id()), notNull(Notification.class));
        verifyNoMoreInteractions(manager);
    }

    public void test_notify_on_failure() throws Exception {

        File file = LocalFile.of("p");
        IOException err = new IOException("test");
        List<Failure> failures = singletonList(Failure.create(file, err));
        provider.onUpdate(context, base
                .running(Time.create(1, 1))
                .failed(Time.create(2, 2), failures));
        // See comment in class under test for this ID
        int id = Integer.MAX_VALUE - base.task().id();
        verify(manager, timeout(1000)).notify(eq(id), notNull(Notification.class));
    }

    public void test_remove_notification_on_unknown_error() throws Exception {

        provider.onUpdate(context, base
                .running(Time.create(1, 1))
                .failed(Time.create(2, 2), Collections.<Failure>emptyList()));
        verify(manager, timeout(1000)).cancel(base.task().id());
    }

    public void test_create_failure_intent_with_correct_failure_data() throws Exception {

        Intent intent = provider.getFailureIntent(context, base
                .running(Time.create(1, 1))
                .failed(Time.create(2, 2), asList(
                        Failure.create(LocalFile.of("1"), new IOException("test1")),
                        Failure.create(LocalFile.of("2"), new IOException("test2")))));
        Collection<FailureMessage> actual = getFailures(intent);
        Collection<FailureMessage> expected = asList(
                FailureMessage.create(LocalFile.of("1"), "test1"),
                FailureMessage.create(LocalFile.of("2"), "test2")
        );
        assertEquals(expected, actual);
        assertTrue(getTitle(intent).matches(".+"));
    }

}
