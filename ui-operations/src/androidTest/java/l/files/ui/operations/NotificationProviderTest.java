package l.files.ui.operations;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import l.files.fs.Path;
import l.files.operations.Failure;
import l.files.operations.Progress;
import l.files.operations.Target;
import l.files.operations.TaskId;
import l.files.operations.TaskNotFound;
import l.files.operations.TaskState;
import l.files.operations.Time;

import static android.support.test.InstrumentationRegistry.getContext;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static l.files.operations.TaskKind.COPY;
import static l.files.ui.operations.FailuresActivity.getFailures;
import static l.files.ui.operations.FailuresActivity.getTitle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public final class NotificationProviderTest {

    private TaskState.Pending base;
    private Context context;
    private NotificationManager manager;
    private NotificationProvider provider;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getAbsolutePath());
        provider = new NotificationProvider();
        base = TaskState.pending(
                TaskId.create(1, COPY),
                Target.from(Collections.<Path>emptyList(), mock(Path.class)),
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

    @Test
    public void cancel_on_task_not_found() throws Exception {

        provider.onNotFound(context, TaskNotFound.create(1011));
        verify(manager).cancel(1011);
    }

    @Test
    public void cancel_notification_on_success() {

        provider.onUpdate(context, base.running(Time.create(1, 1)).success(Time.create(2, 2)));
        verify(manager, timeout(1000)).cancel(base.task().id());
        verifyNoMoreInteractions(manager);
    }

    @Test
    public void notify_on_progress() {

        provider.onUpdate(context, base.running(Time.create(1, 1), Progress.NONE, Progress.NONE));
        verify(manager, timeout(1000)).notify(eq(base.task().id()), notNull(Notification.class));
        verifyNoMoreInteractions(manager);
    }

    @Test
    public void notify_on_failure() throws Exception {

        Path file = mock(Path.class, "p");
        IOException err = new IOException("test");
        List<Failure> failures = singletonList(Failure.create(file, err));
        provider.onUpdate(context, base
                .running(Time.create(1, 1))
                .failed(Time.create(2, 2), failures));
        // See comment in class under test for this ID
        int id = Integer.MAX_VALUE - base.task().id();
        verify(manager, timeout(1000)).notify(eq(id), notNull(Notification.class));
    }

    @Test
    public void remove_notification_on_unknown_error() throws Exception {

        provider.onUpdate(context, base
                .running(Time.create(1, 1))
                .failed(Time.create(2, 2), Collections.<Failure>emptyList()));
        verify(manager, timeout(1000)).cancel(base.task().id());
    }

    @Test
    public void create_failure_intent_with_correct_failure_data() throws Exception {

        Path f1 = mock(Path.class, "1");
        Path f2 = mock(Path.class, "2");

        Intent intent = provider.getFailureIntent(context, base
                .running(Time.create(1, 1))
                .failed(Time.create(2, 2), asList(
                        Failure.create(f1, new IOException("test1")),
                        Failure.create(f2, new IOException("test2")))));

        Collection<FailureMessage> actual = getFailures(intent);
        Collection<FailureMessage> expected = asList(
                FailureMessage.create(f1, "test1"),
                FailureMessage.create(f2, "test2")
        );

        assertEquals(expected, actual);
        assertTrue(getTitle(intent).matches(".+"));
    }

}
