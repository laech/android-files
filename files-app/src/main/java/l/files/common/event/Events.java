package l.files.common.event;

import android.os.Handler;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public final class Events {

  private static final Bus BUS = new Bus(ThreadEnforcer.MAIN) {
    private final Set<Object> objects = newHashSet();

    @Override public void register(Object object) {
      ThreadEnforcer.MAIN.enforce(this);
      if (objects.add(object)) super.register(object);
    }

    @Override public void unregister(Object object) {
      ThreadEnforcer.MAIN.enforce(this);
      if (objects.remove(object)) super.unregister(object);
    }
  };

  /**
   * Returns an event bus configured with {@link ThreadEnforcer#MAIN}.
   */
  public static Bus bus() {
    return BUS;
  }

  public static void post(final Bus bus, final Object event, Handler handler) {
    handler.post(new Runnable() {
      @Override public void run() {
        bus.post(event);
      }
    });
  }

  private Events() {}
}
