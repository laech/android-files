package l.files.operations;

import android.os.Handler;

import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.concurrent.Future;

import de.greenrobot.event.EventBus;
import l.files.common.testing.BaseTest;
import l.files.eventbus.Events;
import l.files.eventbus.Subscribe;
import l.files.fs.local.LocalPath;

import static com.google.common.util.concurrent.MoreExecutors.newDirectExecutorService;
import static java.util.Arrays.asList;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.TaskKind.MOVE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TaskTest extends BaseTest {

  private EventBus bus;
  private Handler handler;

  @Override protected void setUp() throws Exception {
    super.setUp();
    bus = Events.failFast(new EventBus());
    handler = new Handler();
  }

  public void testNotifiesOnCancel() throws Exception {
    ArgumentCaptor<TaskState> captor = capturedExecute(new Command() {
      @Override public void execute() throws InterruptedException {
        throw new InterruptedException("Test");
      }
    });
    assertTrue(captor.getValue() instanceof TaskState.Success);
  }

  public void testNotifiesOnFailure() throws Throwable {
    ArgumentCaptor<TaskState> captor = capturedExecute(new Command() {
      @Override public void execute() throws FileException {
        throw new FileException(asList(
            Failure.create(LocalPath.of("a"), new IOException("Test"))));
      }
    });
    assertTrue(captor.getValue() instanceof TaskState.Failed);
  }

  public void testNotifiesOnStart() throws Exception {
    ArgumentCaptor<TaskState> captor = capturedExecute();
    assertTrue(captor.getAllValues().get(0) instanceof TaskState.Pending);
  }

  public void testNotifiesOnSuccess() throws Exception {
    ArgumentCaptor<TaskState> captor = capturedExecute();
    assertTrue(captor.getValue() instanceof TaskState.Success);
  }

  private ArgumentCaptor<TaskState> capturedExecute() {
    return capturedExecute(new Command() {
      @Override public void execute() {}
    });
  }

  private ArgumentCaptor<TaskState> capturedExecute(final Command command) {
    Listener listener = mock(Listener.class);
    bus.register(listener);
    new TestTask(bus, handler) {
      @Override
      protected void doTask() throws FileException, InterruptedException {
        command.execute();
      }
    }.execute();
    ArgumentCaptor<TaskState> captor = listener.captor();
    verify(listener, timeout(1000).atLeast(1)).onEvent(captor.capture());
    return captor;
  }

  protected Task create(int id, Clock clock, EventBus bus, Handler handler) {
    return new Task(TaskId.create(id, MOVE), Target.NONE, clock, bus, handler) {
      @Override protected void doTask() {}

      @Override protected TaskState.Running running(TaskState.Running state) {
        return state;
      }
    };
  }

  private static abstract class TestTask extends Task {
    TestTask(EventBus bus, Handler handler) {
      super(TaskId.create(0, COPY), Target.NONE, Clock.system(), bus, handler);
    }

    @Override protected TaskState.Running running(TaskState.Running state) {
      return state;
    }

    Future<?> execute() {
      return execute(newDirectExecutorService());
    }
  }

  private static interface Command {
    void execute() throws InterruptedException, FileException;
  }

  public static abstract class Listener {
    public abstract void onEvent(TaskState state);

    final ArgumentCaptor<TaskState> captor() {
      return ArgumentCaptor.forClass(TaskState.class);
    }
  }
}
