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
    String getContentTitlePending(Context context);

    String getContentTitleRunning(Context context, TaskState.Running state);

    String getContentTitleFailed(Context context, TaskState.Failed state);

    // android.app.Notification.Builder#setContentText(CharSequence)
    String getContentText(Context context, TaskState.Running state);

    // android.app.Notification.Builder#setContentInfo(CharSequence)
    String getContentInfo(Context context, TaskState.Running state);

    // android.app.Notification.Builder#setProgress(int, int, boolean)
    float getProgress(TaskState.Running state);

}
