package l.files.event;

import android.app.Application;
import android.content.ClipboardManager;
import com.squareup.otto.Bus;

public final class Events {
  private Events() {}

  public static void registerClipboardProvider(Bus bus, ClipboardManager manager) {
    bus.register(new ClipboardProvider(manager));
  }

  public static void registerIoProvider(Bus bus, Application context) {
    bus.register(new IoProvider(context));
  }
}
