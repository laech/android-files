package l.files.operations;

import android.os.Handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.BaseTest;
import l.files.fs.local.LocalFile;
import l.files.operations.Task.Callback;
import l.files.operations.TaskState.Failed;
import l.files.operations.TaskState.Pending;
import l.files.operations.TaskState.Success;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static android.os.Looper.getMainLooper;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.TaskKind.MOVE;

public final class TaskTest extends BaseTest {

  private Handler handler;

  @Override protected void setUp() throws Exception {
    super.setUp();
    handler = new Handler(getMainLooper());
  }

  public void testNotifiesOnCancelFromInterrupt() throws Exception {
    TaskState state = last(capturedExecute(new Command() {
      @Override public void execute(Task task) throws InterruptedException {
        throw new InterruptedException("Test");
      }
    }));
    assertTrue(state.toString(), state instanceof Success);
  }

  public void testNotifiesOnCancelFromCancellingTask() throws Exception {
    List<TaskState> states = capturedExecute(new Command() {
      @Override public void execute(Task task) throws InterruptedException {
        task.cancel(true);
      }
    });
    assertTrue(states.toString(), last(states) instanceof Success);
  }

  public void testNotifiesOnFailure() throws Throwable {
    TaskState state = last(capturedExecute(new Command() {
      @Override public void execute(Task task) throws FileException {
        throw new FileException(singletonList(Failure.create(
            LocalFile.create(new File("a")), new IOException("Test")
        )));
      }
    }));
    assertTrue(state.toString(), state instanceof Failed);
  }

  public void testNotifiesOnStart() throws Exception {
    TaskState state = capturedExecute().get(0);
    assertTrue(state.toString(), state instanceof Pending);
  }

  public void testNotifiesOnSuccess() throws Exception {
    List<TaskState> states = capturedExecute();
    assertTrue(states.toString(), last(states) instanceof Success);
  }

  private List<TaskState> capturedExecute() throws InterruptedException {
    return capturedExecute(new Command() {
      @Override public void execute(Task task) {}
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

  Task create(int id, Clock clock, Handler handler, Callback callback) {
    return new Task(TaskId.create(id, MOVE), Target.NONE, clock, callback, handler) {
      @Override protected void doTask() {
      }

      @Override protected TaskState.Running running(TaskState.Running state) {
        return state;
      }
    };
  }

  private static abstract class TestTask extends Task {
    TestTask(Handler handler, Callback callback) {
      super(TaskId.create(0, COPY), Target.NONE, Clock.system(), callback, handler);
    }

    @Override protected TaskState.Running running(TaskState.Running state) {
      return state;
    }
  }

  private interface Command {
    void execute(Task task) throws InterruptedException, FileException;
  }

  private static class Listener implements Callback {
    final List<TaskState> states = new ArrayList<>();
    final CountDownLatch latch = new CountDownLatch(1);

    @Override public void onUpdate(TaskState state) {
      states.add(state);
      if (state.isFinished()) {
        latch.countDown();
      }
    }
  }
}
