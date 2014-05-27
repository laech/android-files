package l.files.operations;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public final class EventBus {

  private static final Bus bus = new Bus(ThreadEnforcer.MAIN);

  /**
   * Gets the event bus this module is used to post updates.
   */
  public static Bus get() {
    return bus;
  }

  private EventBus() {}
}
