package l.files.operations;

import android.os.Handler;

import java.util.List;

import de.greenrobot.event.EventBus;

import static android.os.SystemClock.elapsedRealtime;
import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;
import static l.files.operations.TaskInfo.TaskStatus.FINISHED;

abstract class Task implements TaskInfo, Runnable {

  private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

  private final Runnable update = new Runnable() {
    @Override public void run() {
      notifyProgress();
      if (getTaskStatus() != FINISHED) {
        handler.postDelayed(this, PROGRESS_UPDATE_DELAY_MILLIS);
      }
    }
  };

  private final int id;
  private final EventBus bus;
  private final Handler handler;

  private volatile long startTime;
  private volatile long elapsedRealtimeOnRun;
  private volatile TaskStatus status;
  private volatile List<Failure> failures = emptyList();

  protected Task(int id, EventBus bus, Handler handler) {
    this.id = id;
    this.bus = checkNotNull(bus, "bus");
    this.handler = checkNotNull(handler, "handler");
  }

  @Override public void run() {
    onPending();
    try {
      onRunning();
    } finally {
      onFinished();
    }
  }

  private void onPending() {
    status = TaskStatus.PENDING;
    startTime = currentTimeMillis();
    if (!currentThread().isInterrupted()) {
      notifyProgress();
    }
  }

  private void onRunning() {
    status = TaskStatus.RUNNING;
    elapsedRealtimeOnRun = elapsedRealtime();
    handler.postDelayed(update, PROGRESS_UPDATE_DELAY_MILLIS);
    try {
      doTask();
    } catch (InterruptedException e) {
      // Cancelled, let it finish
    } catch (FileException e) {
      failures = e.failures();
    }
  }

  private void onFinished() {
    status = TaskStatus.FINISHED;
    handler.removeCallbacks(update);
    notifyProgress();
  }

  protected abstract void doTask() throws FileException, InterruptedException;

  final void notifyProgress() {
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
    return toStringHelper(this).addValue(getTaskStatus()).toString();
  }
}
