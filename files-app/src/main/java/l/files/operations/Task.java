package l.files.operations;

import android.os.AsyncTask;
import android.os.Handler;

import java.util.Collections;

import l.files.operations.TaskState.Running;

import static java.util.Objects.requireNonNull;

abstract class Task extends AsyncTask<Void, TaskState, Throwable> {

  private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

  private final Runnable update = new Runnable() {
    @Override
    public void run() {
      if (!state.isFinished()) {
        state = running((Running) state);
        callback.onUpdate(state);
        handler.postDelayed(this, PROGRESS_UPDATE_DELAY_MILLIS);
      }
    }
  };

  private final TaskId id;
  private final Target target;
  private final Clock clock;
  private final Handler handler;
  private final Callback callback;

  private volatile TaskState state;

  Task(TaskId id, Target target, Clock clock, Callback callback, Handler handler) {
    this.id = requireNonNull(id);
    this.target = requireNonNull(target);
    this.clock = requireNonNull(clock);
    this.handler = requireNonNull(handler);
    this.callback = requireNonNull(callback);
  }

  @Override protected void onPreExecute() {
    super.onPreExecute();
    state = TaskState.pending(id, target, clock.read());
    callback.onUpdate(state);
  }

  @Override protected Throwable doInBackground(Void... params) {
    try {
      state = ((TaskState.Pending) state).running(clock.read());
      handler.postDelayed(update, PROGRESS_UPDATE_DELAY_MILLIS);
      doTask();
      return null;
    } catch (Throwable e) {
      return e;
    }
  }

  @Override protected void onPostExecute(Throwable e) {
    super.onPostExecute(e);
    handler.removeCallbacks(update);

    if (e == null || e instanceof InterruptedException) {
      // Cancelled, let it finish
      // Use success as the state, may add a cancel state in future if needed
      state = ((Running) state).success(clock.read());
      callback.onUpdate(state);

    } else if (e instanceof FileException) {
      state = ((Running) state).failed(clock.read(), ((FileException) e).failures());
      callback.onUpdate(state);

    } else {
      state = ((Running) state).failed(clock.read(), Collections.<Failure>emptyList());
      callback.onUpdate(state);

      if (e instanceof Error) {
        throw (Error) e;

      } else if (e instanceof RuntimeException) {
        throw (RuntimeException) e;

      } else {
        throw new RuntimeException(e);
      }
    }

  }

  abstract void doTask() throws FileException, InterruptedException;

  abstract Running running(Running state);

  interface Callback {
    void onUpdate(TaskState state);
  }
}
