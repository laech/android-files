package l.files.operations;

import android.os.AsyncTask;
import android.os.Handler;
import l.files.operations.TaskState.Running;

import java.util.Collections;

import static java.util.Objects.requireNonNull;

abstract class Task extends AsyncTask<Void, TaskState, Void> {

    private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

    private final Runnable update = new Runnable() {
        @Override
        public void run() {
            TaskState s = Task.this.state;
            if (!s.isFinished()) {
                s = running((Running) s);
                callback.onUpdate(s);
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

    Task(
        TaskId id,
        Target target,
        Clock clock,
        Callback callback,
        Handler handler
    ) {
        this.id = requireNonNull(id);
        this.target = requireNonNull(target);
        this.clock = requireNonNull(clock);
        this.handler = requireNonNull(handler);
        this.callback = requireNonNull(callback);
        this.state = TaskState.pending(id, target, clock.read());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        state = TaskState.pending(id, target, clock.read());
        callback.onUpdate(state);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {

            state = ((TaskState.Pending) state).running(clock.read());
            handler.postDelayed(update, PROGRESS_UPDATE_DELAY_MILLIS);
            doTask();
            state = ((Running) state).success(clock.read());
            return null;

        } catch (Throwable e) {

            if (e instanceof InterruptedException) {
                state = ((Running) state).success(clock.read());

            } else if (e instanceof FileException) {
                state = ((Running) state).failed(
                    clock.read(),
                    ((FileException) e).failures()
                );

            } else {
                state = ((Running) state).failed(
                    clock.read(),
                    Collections.emptyList()
                );

                if (e instanceof Error) {
                    throw (Error) e;
                } else {
                    throw (RuntimeException) e;
                }
            }

        } finally {
            handler.removeCallbacks(update);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onUpdate(state);
                }
            });
        }

        return null;
    }

    abstract void doTask() throws FileException, InterruptedException;

    abstract Running running(Running state);

    interface Callback {
        void onUpdate(TaskState state);
    }
}
