package l.files.operations.ui.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collection;

import l.files.io.file.operations.FileOperation;
import l.files.operations.info.CopyTaskInfo;
import l.files.operations.info.DeleteTaskInfo;
import l.files.operations.info.MoveTaskInfo;
import l.files.operations.info.TaskInfo;
import l.files.operations.ui.FailureMessage;
import l.files.operations.ui.FailuresActivity;
import l.files.operations.ui.R;

import static android.app.Notification.PRIORITY_LOW;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.content.Context.NOTIFICATION_SERVICE;
import static l.files.operations.OperationService.newCancelIntent;
import static l.files.operations.info.TaskInfo.TaskStatus.FINISHED;
import static l.files.operations.ui.FailuresActivity.getTitle;

/**
 * Handle file operation notification updates.
 */
public class NotificationReceiver {

  private final Context context;
  private final NotificationManager manager;
  private final NotificationViewer<DeleteTaskInfo> deleteViewer;
  private final NotificationViewer<CopyTaskInfo> copyViewer;
  private final NotificationViewer<MoveTaskInfo> moveViewer;

  NotificationReceiver(Context context, NotificationManager manager) {
    this.context = context;
    this.manager = manager;
    this.deleteViewer = new DeleteViewer(context, Clock.SYSTEM);
    this.copyViewer = new CopyViewer(context, Clock.SYSTEM);
    this.moveViewer = new MoveViewer(context, Clock.SYSTEM);
  }

  public static void register(EventBus bus, Context context) {
    register(bus, context, (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE));
  }

  public static void register(EventBus bus, Context context, NotificationManager manager) {
    bus.register(new NotificationReceiver(context, manager));
  }

  @Subscribe public void on(DeleteTaskInfo value) {
    notify(deleteViewer, value, R.plurals.fail_to_delete);
  }

  @Subscribe public void on(CopyTaskInfo value) {
    notify(copyViewer, value, R.plurals.fail_to_copy);
  }

  @Subscribe public void on(MoveTaskInfo value) {
    notify(moveViewer, value, R.plurals.fail_to_move);
  }

  private <T extends TaskInfo> void notify(
      NotificationViewer<T> viewer, T value, int failurePuralTitleId) {
    switch (value.getTaskStatus()) {
      case FINISHED:
        if (value.getFailures().isEmpty()) {
          manager.cancel(value.getTaskId());
        } else {
          onFailure(value, failurePuralTitleId);
        }
        break;
      default:
        manager.notify(value.getTaskId(), newProgressNotification(viewer, value));
        break;
    }
  }

  private <T extends TaskInfo> Notification newProgressNotification(
      NotificationViewer<T> viewer, T value) {
    int progressMax = 10000;
    int percentage = (int) (viewer.getProgress(value) * progressMax);
    boolean indeterminate = percentage <= 0;
    return new Notification.Builder(context)
        .setPriority(PRIORITY_LOW)
        .setContentTitle(viewer.getContentTitle(value).orNull())
        .setContentText(viewer.getContentText(value).orNull())
        .setSmallIcon(viewer.getSmallIcon())
                /*
                 * Set when to a fixed value to prevent flickering on update when there
                 * are multiple notifications being displayed/updated.
                 */
        .setWhen(value.getTaskStartTime())
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .setProgress(progressMax, percentage, indeterminate)
        .setContentInfo(viewer.getContentInfo(value).orNull())
        .addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            context.getString(android.R.string.cancel),
            newCancelIntent(context, value.getTaskId()))
        .build();
  }


  @VisibleForTesting public void onFailure(TaskInfo value, int pluralTitleId) {
    Optional<Intent> intent = getFailureIntent(context, value, pluralTitleId);
    if (intent.isPresent()) {
      PendingIntent pending = getActivity(context, value.getTaskId(), intent.get(), FLAG_UPDATE_CURRENT);
      manager.notify(value.getTaskId(), new Notification.Builder(context)
          .setSmallIcon(android.R.drawable.stat_notify_error)
          .setContentTitle(getTitle(intent.get()))
          .setContentIntent(pending)
          .setAutoCancel(true)
          .build());
    }
  }

  @VisibleForTesting
  static Optional<Intent> getFailureIntent(Context context, TaskInfo value, int pluralTitleId) {
    if (value.getTaskStatus() != FINISHED) {
      return Optional.absent();
    }

    Collection<FileOperation.Failure> failures = value.getFailures();
    if (failures.isEmpty()) {
      return Optional.absent();
    }

    ArrayList<FailureMessage> messages = new ArrayList<>(failures.size());
    for (FileOperation.Failure failure : failures) {
      messages.add(FailureMessage.create(failure.path(), failure.cause().getMessage()));
    }
    String title = context.getResources().getQuantityString(pluralTitleId, value.getFailures().size());
    return Optional.of(FailuresActivity.newIntent(context, title, messages));
  }
}
