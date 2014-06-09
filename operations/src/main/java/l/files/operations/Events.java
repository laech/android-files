package l.files.operations;

import android.os.Handler;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

import java.util.concurrent.Executor;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;

public final class Events {

    private static final Handler handler = new Handler(getMainLooper());

    private static final EventBus bus = new AsyncEventBus(new Executor() {
        @Override
        public void execute(Runnable command) {
            if (getMainLooper() == myLooper()) {
                command.run();
            } else {
                handler.post(command);
            }
        }
    });

    Events() {
    }

    /**
     * Gets the event bus this module uses to post events,
     * all events will be delivered on the main thread.
     * Register/unregister can be called on any thread.
     */
    public static EventBus get() {
        return bus;
    }
}
