package l.files.operations.ui.notification;

/**
 * Provides notification information from a file operation broadcast value.
 */
interface NotificationViewer<T> {

    /**
     * @see android.app.Notification.Builder#setSmallIcon(int)
     */
    int getSmallIcon();

    /**
     * @see android.app.Notification.Builder#setContentTitle(CharSequence)
     */
    String getContentTitle(T value);

    /**
     * @see android.app.Notification.Builder#setContentText(CharSequence)
     */
    String getContentText(T value);

    /**
     * @see android.app.Notification.Builder#setContentInfo(CharSequence)
     */
    String getContentInfo(T value);

    /**
     * Gets the current progress (between 0 and 1 where 0 is nothing is done).
     * Return -1 to indicate progress is indeterminate.
     *
     * @see android.app.Notification.Builder#setProgress(int, int, boolean)
     */
    float getProgress(T value);
}
