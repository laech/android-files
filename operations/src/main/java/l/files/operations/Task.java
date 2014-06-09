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

    private final int id;
    private volatile long startTime;
    private volatile long elapsedStartTime;
    private volatile TaskStatus status;

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
