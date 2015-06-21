package l.files.operations;

import android.os.Handler;

import com.google.common.base.Throwables;

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
            final TaskId id,
            final Target target,
            final Clock clock,
            final EventBus bus,
            final Handler handler) {
        this.id = requireNonNull(id, "id");
        this.target = requireNonNull(target, "target");
        this.clock = requireNonNull(clock, "clock");
        this.bus = requireNonNull(bus, "bus");
        this.handler = requireNonNull(handler, "handler");
    }

    public Future<?> execute(final ExecutorService executor) {
        onPending();
        return executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    onRunning();
                } catch (final Throwable e) {
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
            @Override
            public void run() {
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
        } catch (final InterruptedException e) {
            // Cancelled, let it finish
            // Use success as the state, may add a cancel state in future if needed
            state = ((TaskState.Running) state).success(clock.read());
        } catch (final FileException e) {
            state = ((TaskState.Running) state).failed(clock.read(), e.failures());
        } catch (final RuntimeException e) {
            state = ((TaskState.Running) state).failed(clock.read(), Collections.<Failure>emptyList());
            throw e;
        }
    }

    private void onFinished() {
        handler.removeCallbacks(update);
        notifyProgress(state);
    }

    protected abstract void doTask() throws FileException, InterruptedException;

    protected abstract TaskState.Running running(TaskState.Running state);

    final void notifyProgress(final TaskState state) {
        bus.post(state);
    }

}
