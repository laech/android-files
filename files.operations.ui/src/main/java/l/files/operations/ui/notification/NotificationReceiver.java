package l.files.operations.ui.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import l.files.operations.Progress;

import static android.content.Context.NOTIFICATION_SERVICE;
import static l.files.operations.OperationService.newCancelIntent;
import static l.files.operations.Progress.STATUS_FINISHED;
import static l.files.operations.Progress.getTaskId;
import static l.files.operations.Progress.getTaskStartTime;
import static l.files.operations.Progress.getTaskStatus;

/**
 * Handle file operation notification updates.
 */
public final class NotificationReceiver extends BroadcastReceiver {

  private static Map<String, NotificationViewer> viewers;

  public static void register(LocalBroadcastManager manager) {
    IntentFilter filter = new IntentFilter();
    filter.addAction(Progress.Delete.ACTION);
    manager.registerReceiver(new NotificationReceiver(), filter);
  }

  @Override public void onReceive(Context context, Intent intent) {
    if (viewers == null) {
      Resources res = context.getApplicationContext().getResources();
      viewers = ImmutableMap.<String, NotificationViewer>of(
          Progress.Delete.ACTION, new DeleteViewer(res)
      );
    }
    notify(context, intent);
  }

  private void notify(Context context, Intent intent) {
    NotificationManager manager = (NotificationManager)
        context.getSystemService(NOTIFICATION_SERVICE);

    switch (getTaskStatus(intent)) {
      case STATUS_FINISHED:
        manager.cancel(getTaskId(intent));
        break;
      default:
        manager.notify(getTaskId(intent), newNotification(context, intent));
        break;
    }
  }

  private Notification newNotification(Context context, Intent intent) {
    NotificationViewer viewer = viewers.get(intent.getAction());
    int progressMax = 10000;
    int percentage = (int) (viewer.getProgress(intent) * progressMax);
    boolean indeterminate = percentage == 0;
    return new Notification.Builder(context)
        .setContentTitle(viewer.getContentTitle(intent))
        .setContentText(viewer.getContentText(intent))
        .setSmallIcon(viewer.getSmallIcon())
          /*
           * Set when to a fixed value to prevent flickering on update when there
           * are multiple notifications being displayed/updated.
           */
        .setWhen(getTaskStartTime(intent))
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .setProgress(progressMax, percentage, indeterminate)
        .setContentInfo(viewer.getContentInfo(intent))
        .addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            context.getString(android.R.string.cancel),
            newCancelIntent(context, getTaskId(intent)))
        .build();
  }
}
