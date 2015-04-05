package l.files.operations;

import android.os.Handler;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.greenrobot.event.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Thread.currentThread;

abstract class Task {

  private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

  private final Runnable update = new Runnable() {
    @Override public void run() {
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

  Task(TaskId id, Target target, Clock clock, EventBus bus, Handler handler) {
    this.id = checkNotNull(id, "id");
    this.target = checkNotNull(target, "target");
    this.clock = checkNotNull(clock, "clock");
    this.bus = checkNotNull(bus, "bus");
    this.handler = checkNotNull(handler, "handler");
  }

  Future<?> execute(ExecutorService executor) {
    onPending();
    return executor.submit(new Runnable() {
      @Override public void run() {
        try {
          onRunning();
        } catch (Throwable e) {
          throwToMainThread(e);
          throw e;
        } finally {
          onFinished();
        }
      }
    });
  }

  private void throwToMainThread(final Throwable e) {
    handler.post(new Runnable() {
      @Override public void run() {
        Throwables.propagate(e);
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
      state = ((TaskState.Running) state).failed(clock.read(), ImmutableList.<Failure>of());
      throw e;
    }
  }

  private void onFinished() {
    handler.removeCallbacks(update);
    notifyProgress(state);
  }

  protected abstract void doTask() throws FileException, InterruptedException;

  protected abstract TaskState.Running running(TaskState.Running state);

  final void notifyProgress(TaskState state) {
    bus.post(state);
  }

}
