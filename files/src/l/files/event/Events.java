package l.files.event;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public final class Events {

  private static final Bus BUS = new Bus(ThreadEnforcer.MAIN);

  /**
   * Gets the main thread event bus.
   */
  public static Bus bus() {
    return BUS;
  }

  private Events() {}
}
