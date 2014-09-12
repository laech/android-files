package l.files.operations.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Collection;

import l.files.eventbus.Subscribe;
import l.files.operations.CopyTaskInfo;
import l.files.operations.DeleteTaskInfo;
import l.files.operations.Failure;
import l.files.operations.MoveTaskInfo;
import l.files.operations.TaskInfo;

import static android.app.Notification.PRIORITY_LOW;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.google.common.base.Preconditions.checkNotNull;
import static de.greenrobot.event.ThreadMode.MainThread;
import static l.files.operations.OperationService.newCancelIntent;
import static l.files.operations.TaskInfo.TaskStatus.FINISHED;
import static l.files.operations.ui.FailuresActivity.getTitle;

/**
 * Handle file operation notification updates.
 */
class NotificationReceiver {

  private final Context context;
  private final NotificationManager manager;
  private final NotificationViewer<DeleteTaskInfo> deleteViewer;
  private final NotificationViewer<CopyTaskInfo> copyViewer;
  private final NotificationViewer<MoveTaskInfo> moveViewer;

  public NotificationReceiver(Context context) {
    this(context, (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE));
  }

  NotificationReceiver(Context context, NotificationManager manager) {
    this.context = checkNotNull(context, "context");
    this.manager = checkNotNull(manager, "manager");
    this.deleteViewer = new DeleteViewer(context, Clock.SYSTEM);
    this.copyViewer = new CopyViewer(context, Clock.SYSTEM);
    this.moveViewer = new MoveViewer(context, Clock.SYSTEM);
  }

  @Subscribe(MainThread)
  public void onEventMainThread(DeleteTaskInfo value) {
    notify(deleteViewer, value, R.plurals.fail_to_delete);
  }

  @Subscribe(MainThread)
  public void onEventMainThread(CopyTaskInfo value) {
    notify(copyViewer, value, R.plurals.fail_to_copy);
  }

  @Subscribe(MainThread)
  public void onEventMainThread(MoveTaskInfo value) {
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

    Collection<Failure> failures = value.getFailures();
    if (failures.isEmpty()) {
      return Optional.absent();
    }

    ArrayList<FailureMessage> messages = new ArrayList<>(failures.size());
    for (Failure failure : failures) {
      messages.add(FailureMessage.create(failure.path(), failure.cause().getMessage()));
    }
    String title = context.getResources().getQuantityString(pluralTitleId, value.getFailures().size());
    return Optional.of(FailuresActivity.newIntent(context, title, messages));
  }
}
