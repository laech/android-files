package l.files.operations;

import android.os.Handler;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.greenrobot.event.EventBus;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

abstract class Task {

  private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

  private final Runnable update = new Runnable() {
    @Override
    public void run() {
      if (!state.isFinished()) {
        state = running((TaskState.Running) state);
        notifyProgress(state);
        handler.postDelayed(this, PROGRESS_UPDATE_DELAY_MILLIS);
      }
    }
  };

  private final TaskId id;
  private final Target target;
  private final Clock clock;
  private final EventBus bus;
  private final Handler handler;

  private volatile TaskState state;

  public Task(
      TaskId id,
      Target target,
      Clock clock,
      EventBus bus,
      Handler handler) {
    this.id = requireNonNull(id, "id");
    this.target = requireNonNull(target, "target");
    this.clock = requireNonNull(clock, "clock");
    this.bus = requireNonNull(bus, "bus");
    this.handler = requireNonNull(handler, "handler");
  }

  public Future<?> execute(ExecutorService executor) {
    onPending();
    return executor.submit(new Runnable() {
      @Override public void run() {
        try {
          onRunning();
        } finally {
          onFinished();
        }
      }
    });
  }

  public TaskState state() {
    return state;
  }

  private void onPending() {
    state = TaskState.pending(id, target, clock.read());
    if (!currentThread().isInterrupted()) {
      notifyProgress(state);
    }
  }

  private void onRunning() {
    try {
      state = ((TaskState.Pending) state).running(clock.read());
      handler.postDelayed(update, PROGRESS_UPDATE_DELAY_MILLIS);
      doTask();
      state = ((TaskState.Running) state).success(clock.read());
    } catch (InterruptedException e) {
      // Cancelled, let it finish
      // Use success as the state, may add a cancel state in future if needed
      state = ((TaskState.Running) state).success(clock.read());
    } catch (FileException e) {
      state = ((TaskState.Running) state).failed(clock.read(), e.failures());
    } catch (RuntimeException e) {
      state = ((TaskState.Running) state).failed(clock.read(), Collections.<Failure>emptyList());
      throw e;
    }
  }

  private void onFinished() {
    handler.removeCallbacks(update);
    notifyProgress(state);
  }

  abstract void doTask() throws FileException, InterruptedException;

  abstract TaskState.Running running(TaskState.Running state);

  final void notifyProgress(TaskState state) {
    bus.post(state);
  }

}
