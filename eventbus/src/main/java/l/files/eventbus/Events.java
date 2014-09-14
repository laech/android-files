package l.files.eventbus;

import android.os.Handler;
import android.os.Looper;

import com.google.common.base.Throwables;

import java.util.concurrent.Executor;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.SubscriberExceptionEvent;

public final class Events {
  private Events() {}

  /**
   * When a {@link SubscriberExceptionEvent} is thrown, call the given executor
   * with a runnable that simply rethrows the throwable.
   */
  public static EventBus failFast(EventBus bus, final Executor executor) {
    bus.register(new Object() {
      @Subscribe public void onEvent(final SubscriberExceptionEvent event) {
        executor.execute(new Runnable() {
          @Override public void run() {
            Throwables.propagate(event.throwable);
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
      @Override public void execute(Runnable command) {
        handler.post(command);
      }
    });
  }

  /**
   * When a {@link SubscriberExceptionEvent} is thrown, post to the main thread
   * a runnable that simply rethrows the throwable.
   */
  public static EventBus failFast(EventBus bus) {
    return failFast(bus, new Handler(Looper.getMainLooper()));
  }

}
