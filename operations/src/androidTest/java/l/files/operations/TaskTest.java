package l.files.operations;

import android.os.Handler;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.operations.Task.Callback;
import l.files.operations.TaskState.Failed;
import l.files.operations.TaskState.Pending;
import l.files.operations.TaskState.Success;
import l.files.testing.BaseTest;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static android.os.Looper.getMainLooper;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.operations.TaskKind.COPY;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class TaskTest extends BaseTest {

    private Handler handler;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        handler = new Handler(getMainLooper());
    }

    @Test
    public void notifiesOnCancelFromInterrupt() throws Exception {
        TaskState state = last(capturedExecute(new Command() {
            @Override
            public void execute(Task task) throws InterruptedException {
                throw new InterruptedException("Test");
            }
        }));
        assertTrue(state.toString(), state instanceof Success);
    }

    @Test
    public void notifiesOnCancelFromCancellingTask() throws Exception {
        List<TaskState> states = capturedExecute(new Command() {
            @Override
            public void execute(Task task) throws InterruptedException {
                task.cancel(true);
            }
        });
        assertTrue(states.toString(), last(states) instanceof Success);
    }

    @Test
    public void notifiesOnFailure() throws Throwable {
        TaskState state = last(capturedExecute(new Command() {
            @Override
            public void execute(Task task) throws FileException {
                throw new FileException(singletonList(Failure.create(
                        Paths.get("a"), new IOException("Test")
                )));
            }
        }));
        assertTrue(state.toString(), state instanceof Failed);
    }

    @Test
    public void notifiesOnStart() throws Exception {
        TaskState state = capturedExecute().get(0);
        assertTrue(state.toString(), state instanceof Pending);
    }

    @Test
    public void notifiesOnSuccess() throws Exception {
        List<TaskState> states = capturedExecute();
        assertTrue(states.toString(), last(states) instanceof Success);
    }

    private List<TaskState> capturedExecute() throws InterruptedException {
        return capturedExecute(new Command() {
            @Override
            public void execute(Task task) {
            }
        });
    }

    private TaskState last(List<TaskState> states) {
        return states.get(states.size() - 1);
    }

    private List<TaskState> capturedExecute(final Command command) throws InterruptedException {
        Listener listener = new Listener();
        new TestTask(handler, listener) {
            @Override
            protected void doTask() throws FileException, InterruptedException {
                command.execute(this);
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);
        assertTrue(listener.latch.await(1, SECONDS));
        return listener.states;
    }

    private static abstract class TestTask extends Task {
        TestTask(Handler handler, Callback callback) {
            super(
                    TaskId.create(0, COPY),
                    Target.from(Collections.<Path>emptyList(), mock(Path.class)),
                    Clock.system(),
                    callback,
                    handler
            );
        }

        @Override
        protected TaskState.Running running(TaskState.Running state) {
            return state;
        }
    }

    private interface Command {
        void execute(Task task) throws InterruptedException, FileException;
    }

    private static class Listener implements Callback {
        final List<TaskState> states = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void onUpdate(TaskState state) {
            states.add(state);
            if (state.isFinished()) {
                latch.countDown();
            }
        }
    }
}
