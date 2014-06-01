package l.files.operations;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.util.Collections;
import java.util.List;

import l.files.io.file.operations.FileException;

import static android.os.SystemClock.elapsedRealtime;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.currentTimeMillis;
import static l.files.io.file.operations.FileOperation.Failure;

abstract class Task extends AsyncTask<Object, Intent, List<Failure>> {

  private static final Handler handler = new Handler(Looper.getMainLooper());

  private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

  private final Context context;
  private final int id;

  private volatile long elapsedTimeOnStart;
  private volatile long startTime;

  private long lastProgressTime;

  protected Task(Context context, int id) {
    this.context = checkNotNull(context, "context");
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

  protected final long elapsedTimeOnStart() {
    return elapsedTimeOnStart;
  }

  /**
   * Gets the message to post while the task is {@link Progress#STATUS_PENDING}.
   */
  protected abstract Intent getPendingMessage();

  /**
   * Gets the message to post when the task is {@link Progress#STATUS_FINISHED}.
   *
   * @param result the result of execution, empty if none
   */
  protected abstract Intent getFinishedMessage(List<Failure> result);

  @Override protected final void onPreExecute() {
    startTime = currentTimeMillis();
    elapsedTimeOnStart = elapsedRealtime();
    if (!isCancelled()) {
      send(getPendingMessage());
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

  @Override protected final void onProgressUpdate(Intent... values) {
    super.onProgressUpdate(values);
    if (!isCancelled()) {
      for (Intent value : values) {
        send(value);
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
      send(getFinishedMessage(Collections.<Failure>emptyList()));
    } else {
      send(getFinishedMessage(result));
    }
  }

  private void send(Intent value) {
    context.sendBroadcast(value, Permissions.RECEIVE_PROGRESS);
  }
}
