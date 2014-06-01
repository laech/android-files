package l.files.operations.ui;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import l.files.operations.ui.notification.NotificationReceiver;

public final class OperationsUi {
  private OperationsUi() {}

  /**
   * Initializes this module on application startup.
   */
  public static void init(Context context) {
    NotificationReceiver.register(LocalBroadcastManager.getInstance(context));
  }
}
