package l.files.eventbus;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.SubscriberExceptionEvent;

public final class Events {
    private Events() {
    }

    /**
     * When a {@link SubscriberExceptionEvent} is thrown, call the given
     * executor with a runnable that simply rethrows the throwable.
     */
    public static EventBus failFast(EventBus bus, final Executor executor) {
        bus.register(new Object() {
            public void onEvent(final SubscriberExceptionEvent event) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Throwable e = event.throwable;
                        if (e instanceof Error) {
                            throw (Error) e;
                        }
                        if (e instanceof RuntimeException) {
                            throw (RuntimeException) e;
                        }
                        throw new RuntimeException(e);
                    }
                });
            }
        });
        return bus;
    }

    /**
     * When a {@link SubscriberExceptionEvent} is thrown, call the given handler
     * with a runnable that simply rethrows the throwable.
     */
    public static EventBus failFast(EventBus bus, final Handler handler) {
        //noinspection NullableProblems
        return failFast(bus, new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        });
    }

    /**
     * When a {@link SubscriberExceptionEvent} is thrown, post to the main
     * thread a runnable that simply rethrows the throwable.
     */
    public static EventBus failFast(EventBus bus) {
        return failFast(bus, new Handler(Looper.getMainLooper()));
    }

}
