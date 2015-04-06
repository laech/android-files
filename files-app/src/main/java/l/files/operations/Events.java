package l.files.operations;

import android.os.Handler;
import android.os.Looper;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.SubscriberExceptionEvent;

import static java.util.Objects.requireNonNull;

// TODO replace this with dependency injection
public final class Events {

  private static final EventBus bus = EventBus.getDefault();

  static {
    bus.register(new Crasher(new Handler(Looper.getMainLooper())));
  }

  Events() {
  }

  public static EventBus get() {
    return bus;
  }

  /**
   * Make any {@link SubscriberExceptionEvent} visible by crashing the app.
   */
  static final class Crasher {

    private final Handler handler;

    Crasher(Handler handler) {
      this.handler = requireNonNull(handler, "handler");
    }

    public void onEvent(final SubscriberExceptionEvent e) {
      handler.post(new Runnable() {
        @Override public void run() {
          throw new RuntimeException(e.throwable);
        }
      });
    }
  }
}
