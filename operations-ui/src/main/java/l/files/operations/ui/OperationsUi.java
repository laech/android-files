package l.files.operations.ui;

import android.content.Context;

import l.files.operations.Events;
import l.files.operations.ui.notification.FailureReceiver;
import l.files.operations.ui.notification.NotificationReceiver;

public final class OperationsUi {
  private OperationsUi() {}

  /**
   * Initializes this module on application startup.
   */
  public static void init(Context context) {
    NotificationReceiver.register(context, Events.get());
    FailureReceiver.register(Events.get(), context);
  }
}
