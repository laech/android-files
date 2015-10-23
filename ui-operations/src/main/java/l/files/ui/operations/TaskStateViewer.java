package l.files.ui.operations;

import android.content.Context;

import l.files.operations.TaskState;

/**
 * Provides notification information from a file operation broadcast value.
 */
interface TaskStateViewer {

    // android.app.Notification.Builder#setSmallIcon(int)
    int getSmallIcon(Context context);

    // android.app.Notification.Builder#setContentTitle(CharSequence)
    String getContentTitle(Context context, TaskState.Pending state);

    String getContentTitle(Context context, TaskState.Running state);

    String getContentTitle(Context context, TaskState.Failed state);

    // android.app.Notification.Builder#setContentText(CharSequence)
    String getContentText(Context context, TaskState.Running state);

    // android.app.Notification.Builder#setContentInfo(CharSequence)
    String getContentInfo(Context context, TaskState.Running state);

    // android.app.Notification.Builder#setProgress(int, int, boolean)
    float getProgress(Context context, TaskState.Running state);

}
