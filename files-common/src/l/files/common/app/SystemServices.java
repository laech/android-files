package l.files.common.app;

import android.app.NotificationManager;
import android.content.Context;

import static android.content.Context.NOTIFICATION_SERVICE;

public final class SystemServices {
  private SystemServices() {}

  public static NotificationManager getNotificationManager(Context context) {
    return (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
  }
}
