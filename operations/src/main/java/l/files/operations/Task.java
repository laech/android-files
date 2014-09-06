package l.files.operations;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

import de.greenrobot.event.EventBus;
import l.files.io.file.operations.FileException;
import l.files.operations.info.TaskInfo;

import static android.os.SystemClock.elapsedRealtime;
import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static l.files.io.file.operations.FileOperation.Failure;

abstract class Task extends AsyncTask<Object, Object, List<Failure>> implements TaskInfo {

  private static final Handler handler = new Handler(Looper.getMainLooper());

  private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

  private final Runnable update = new Runnable() {
    @Override public void run() {
      notifyProgress();
      if (getStatus() != Status.FINISHED) {
        handler.postDelayed(this, PROGRESS_UPDATE_DELAY_MILLIS);
      }
    }
  };

  private final int id;
  private final EventBus bus;
  private volatile long startTime;
  private volatile long elapsedRealtimeOnRun;
  private volatile TaskStatus status;
  private volatile List<Failure> failures = emptyList();

  protected Task(int id, EventBus bus) {
    this.id = id;
    this.bus = checkNotNull(bus, "bus");
  }

  @Override protected final void onPreExecute() {
    status = TaskStatus.PENDING;
    startTime = currentTimeMillis();
    if (!isCancelled()) {
      notifyProgress();
    }
  }

  @Override protected final List<Failure> doInBackground(Object... params) {
    elapsedRealtimeOnRun = elapsedRealtime();
    handler.postDelayed(update, PROGRESS_UPDATE_DELAY_MILLIS);
    try {
      status = TaskStatus.RUNNING;
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

  protected abstract void doTask() throws FileException, InterruptedException;

  @Override protected final void onPostExecute(List<Failure> result) {
    onDone(result);
  }

  @Override protected final void onCancelled(List<Failure> result) {
    onDone(result);
  }

  private void onDone(List<Failure> result) {
    failures = result;
    status = TaskStatus.FINISHED;
    handler.removeCallbacks(update);
    notifyProgress();
  }

  protected final void notifyProgress() {
    bus.post(this);
  }

  @Override public int getTaskId() {
    return id;
  }

  @Override public long getTaskStartTime() {
    return startTime;
  }

  @Override public long getElapsedRealtimeOnRun() {
    return elapsedRealtimeOnRun;
  }

  @Override public TaskStatus getTaskStatus() {
    return status;
  }

  @Override public List<Failure> getFailures() {
    return failures;
  }

  @Override public String toString() {
    return toStringHelper(this).addValue(getStatus()).toString();
  }
}
