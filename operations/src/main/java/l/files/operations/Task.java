package l.files.operations;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

import l.files.io.file.operations.FileException;
import l.files.operations.info.TaskInfo;

import static android.os.SystemClock.elapsedRealtime;
import static java.lang.System.currentTimeMillis;
import static l.files.io.file.operations.FileOperation.Failure;

abstract class Task extends AsyncTask<Object, Object, List<Failure>> implements TaskInfo {

    private static final Handler handler = new Handler(Looper.getMainLooper());

    private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

    private final Runnable update = new Runnable() {
        @Override
        public void run() {
            notifyProgress();
            if (getStatus() != Status.FINISHED) {
                handler.postDelayed(this, PROGRESS_UPDATE_DELAY_MILLIS);
            }
        }
    };

    private final int id;
    private volatile long startTime;
    private volatile long elapsedStartTime;
    private volatile TaskStatus status;

    protected Task(int id) {
        this.id = id;
    }

    @Override
    protected final void onPreExecute() {
        status = TaskStatus.PENDING;
        startTime = currentTimeMillis();
        elapsedStartTime = elapsedRealtime();
        if (!isCancelled()) {
            notifyProgress();
        }
    }

    @Override
    protected final List<Failure> doInBackground(Object... params) {
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
                @Override
                public void run() {
                    onDone(null);
                }
            });
            throw e;
        }
        return null;
    }

    protected abstract void doTask() throws InterruptedException;

    @Override
    protected final void onPostExecute(List<Failure> result) {
        onDone(result);
    }

    @Override
    protected final void onCancelled(List<Failure> result) {
        onDone(result);
    }

    private void onDone(List<Failure> result) {
        // TODO handle result
        status = TaskStatus.FINISHED;
        handler.removeCallbacks(update);
        notifyProgress();
    }

    protected final void notifyProgress() {
        Events.get().post(this);
    }

    @Override
    public int getTaskId() {
        return id;
    }

    @Override
    public long getTaskStartTime() {
        return startTime;
    }

    @Override
    public long getTaskElapsedStartTime() {
        return elapsedStartTime;
    }

    @Override
    public TaskStatus getTaskStatus() {
        return status;
    }
}
