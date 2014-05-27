package l.files.operations;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.util.Collections;
import java.util.List;

import l.files.io.file.operations.FileException;

import static java.lang.System.currentTimeMillis;
import static l.files.io.file.operations.FileOperation.Failure;
import static l.files.operations.Progress.State;

abstract class Task extends AsyncTask<Object, Object, List<Failure>> {

  private static final Handler handler = new Handler(Looper.getMainLooper());

  private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

  private final int id;
  private volatile long startTime;
  private long lastProgressTime;

  protected Task(int id) {
    this.id = id;
  }

  /**
   * This should be checked before publishing a progress update to check if it
   * has been long enough since publishing the previous progress message, if so,
   * this records the current time and return true to allow the next message to
   * be sent, otherwise returns false - no message should be sent.
   */
  protected final boolean setAndGetUpdateProgress() {
    long time = currentTimeMillis();
    synchronized (this) {
      if (time - lastProgressTime >= PROGRESS_UPDATE_DELAY_MILLIS) {
        lastProgressTime = time;
        return true;
      }
      return false;
    }
  }

  protected final int id() {
    return id;
  }

  protected final long startTime() {
    return startTime;
  }

  /**
   * Gets the message to post while the task is {@link State#PENDING}.
   */
  protected abstract Object getPendingMessage();

  /**
   * Gets the message to post when the task is {@link State#FINISHED}.
   *
   * @param result the result of execution, empty if none
   */
  protected abstract Object getFinishedMessage(List<Failure> result);

  @Override protected final void onPreExecute() {
    startTime = System.currentTimeMillis();
    if (!isCancelled()) {
      post(getPendingMessage());
    }
  }

  @Override protected final List<Failure> doInBackground(Object... params) {
    try {
      doTask();
    } catch (InterruptedException e) {
      // Cancelled, let it finish
    } catch (FileException e) {
      return e.failures();
    } catch (RuntimeException e) {
      handler.post(new Runnable() {
        @Override public void run() {
          onDone(null);
        }
      });
      throw e;
    }
    return null;
  }

  protected abstract void doTask() throws InterruptedException;

  @Override protected final void onProgressUpdate(Object... values) {
    super.onProgressUpdate(values);
    if (!isCancelled()) {
      for (Object value : values) {
        post(value);
      }
    }
  }

  @Override protected final void onPostExecute(List<Failure> result) {
    onDone(result);
  }

  @Override protected final void onCancelled(List<Failure> result) {
    onDone(result);
  }

  private void onDone(List<Failure> result) {
    if (result == null) {
      post(getFinishedMessage(Collections.<Failure>emptyList()));
    } else {
      post(getFinishedMessage(result));
    }
  }

  private void post(Object value) {
    EventBus.get().post(value);
  }
}
