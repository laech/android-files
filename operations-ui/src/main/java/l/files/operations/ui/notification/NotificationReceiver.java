package l.files.operations.ui.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import l.files.operations.info.DeleteTaskInfo;
import l.files.operations.info.TaskInfo;

import static android.app.Notification.PRIORITY_LOW;
import static android.content.Context.NOTIFICATION_SERVICE;
import static l.files.operations.OperationService.newCancelIntent;

/**
 * Handle file operation notification updates.
 */
public final class NotificationReceiver {

    private final Context context;
    private final NotificationViewer<DeleteTaskInfo> deleteViewer;

    NotificationReceiver(Context context) {
        this.context = context;
        this.deleteViewer = new DeleteViewer(context, Clock.SYSTEM);
    }

    public static void register(Context context, EventBus bus) {
        bus.register(new NotificationReceiver(context));
    }

    @Subscribe
    public void on(DeleteTaskInfo value) {
        notify(deleteViewer, value);
    }

    private <T extends TaskInfo> void notify(NotificationViewer<T> viewer, T value) {
        NotificationManager manager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);

        switch (value.getTaskStatus()) {
            case FINISHED:
                manager.cancel(value.getTaskId());
                break;
            default:
                manager.notify(value.getTaskId(), newNotification(viewer, value));
                break;
        }
    }

    private <T extends TaskInfo> Notification newNotification(
            NotificationViewer<T> viewer, T value) {

        int progressMax = 10000;
        int percentage = (int) (viewer.getProgress(value) * progressMax);
        boolean indeterminate = percentage == 0;
        return new Notification.Builder(context)
                .setPriority(PRIORITY_LOW)
                .setContentTitle(viewer.getContentTitle(value))
                .setContentText(viewer.getContentText(value))
                .setSmallIcon(viewer.getSmallIcon())
                /*
                 * Set when to a fixed value to prevent flickering on update when there
                 * are multiple notifications being displayed/updated.
                 */
                .setWhen(value.getTaskStartTime())
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setProgress(progressMax, percentage, indeterminate)
                .setContentInfo(viewer.getContentInfo(value))
                .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        context.getString(android.R.string.cancel),
                        newCancelIntent(context, value.getTaskId()))
                .build();
    }
}
