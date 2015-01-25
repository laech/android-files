package l.files.operations.ui;

import android.content.Context;

import l.files.operations.TaskState;

/**
 * Provides notification information from a file operation broadcast value.
 */
interface TaskStateViewer {

  // android.app.Notification.Builder#setSmallIcon(int)
  int getSmallIcon(Context context);

  // android.app.Notification.Builder#setContentTitle(CharSequence)
  String getContentTitle(TaskState.Pending state);
  String getContentTitle(TaskState.Running state);
  String getContentTitle(TaskState.Failed state);

  // android.app.Notification.Builder#setContentText(CharSequence)
  String getContentText(TaskState.Running state);

  // android.app.Notification.Builder#setContentInfo(CharSequence)
  String getContentInfo(TaskState.Running state);

  // android.app.Notification.Builder#setProgress(int, int, boolean)
  float getProgress(TaskState.Running state);

}
