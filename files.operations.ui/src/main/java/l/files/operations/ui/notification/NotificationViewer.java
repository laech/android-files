package l.files.operations.ui.notification;

import android.content.Intent;

/**
 * Provides notification information from a file operation broadcast intent.
 */
interface NotificationViewer {

  /**
   * @see android.app.Notification.Builder#setSmallIcon(int)
   */
  int getSmallIcon();

  /**
   * @see android.app.Notification.Builder#setContentTitle(CharSequence)
   */
  String getContentTitle(Intent intent);

  /**
   * @see android.app.Notification.Builder#setContentText(CharSequence)
   */
  String getContentText(Intent intent);

  /**
   * @see android.app.Notification.Builder#setContentInfo(CharSequence)
   */
  String getContentInfo(Intent intent);

  /**
   * Gets the current progress (between 0 and 1 where 0 is nothing is done).
   * Return -1 to indicate progress is indeterminate.
   *
   * @see android.app.Notification.Builder#setProgress(int, int, boolean)
   */
  float getProgress(Intent intent);
}
