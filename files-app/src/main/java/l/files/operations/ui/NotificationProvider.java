package l.files.operations.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import l.files.eventbus.Subscribe;
import l.files.operations.Clock;
import l.files.operations.TaskKind;
import l.files.operations.TaskNotFound;
import l.files.operations.TaskState;

import static android.app.Notification.PRIORITY_LOW;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.operations.OperationService.newCancelPendingIntent;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.TaskKind.DELETE;
import static l.files.operations.TaskKind.MOVE;
import static l.files.operations.ui.FailuresActivity.getTitle;

final class NotificationProvider {

  private final Context context;
  private final NotificationManager manager;
  private final Map<TaskKind, ProgressViewer> viewers;

  public NotificationProvider(Context context, Clock clock) {
    this(context, clock, (NotificationManager)
        context.getSystemService(NOTIFICATION_SERVICE));
  }

  NotificationProvider(
      Context context, Clock clock, NotificationManager manager) {
    this.context = checkNotNull(context, "context");
    this.manager = checkNotNull(manager, "manager");
    this.viewers = ImmutableMap.of(
        MOVE, new MoveViewer(context, clock),
        COPY, new CopyViewer(context, clock),
        DELETE, new DeleteViewer(context, clock)
    );
  }

  @Subscribe public void onEvent(TaskState.Pending state) {
    manager.notify(state.task().id(), newIndeterminateNotification(state));
  }

  @Subscribe public void onEvent(TaskState.Running state) {
    manager.notify(state.task().id(), newProgressNotification(state));
  }

  @Subscribe public void onEvent(TaskState.Failed state) {
    manager.cancel(state.task().id());
    if (!state.failures().isEmpty()) {
      // This is the last notification we will display for this task, and it
      // needs to stay until the user dismissed it, can't use the task ID as
      // the notification as when the service finishes, it will bring down the
      // startForeground notification with it.
      int id = Integer.MAX_VALUE - state.task().id();
      manager.notify(id, newFailureNotification(state));
    }
    // If no file failures in collection, then failure is caused by some other
    // errors, let other process handle that error, remove the notification
  }

  @Subscribe public void onEvent(TaskState.Success state) {
    manager.cancel(state.task().id());
  }

  @Subscribe public void onEvent(TaskNotFound event) {
    manager.cancel(event.id());
  }

  private TaskStateViewer getViewer(TaskState state) {
    TaskStateViewer viewer = viewers.get(state.task().kind());
    if (viewer == null) {
      throw new AssertionError(state);
    }
    return viewer;
  }

  private Notification newIndeterminateNotification(TaskState.Pending state) {
    String title = getViewer(state).getContentTitle(state);
    return newIndeterminateNotification(state, title);
  }

  private Notification newIndeterminateNotification(TaskState s, String title) {
    return newProgressNotificationBuilder(s)
        .setContentTitle(title)
        .setProgress(1, 0, true)
        .build();
  }

  private Notification newProgressNotification(TaskState.Running state) {
    TaskStateViewer viewer = getViewer(state);
    if (state.items().isDone() || state.bytes().isDone()) {
      return newIndeterminateNotification(state, viewer.getContentTitle(state));
    }
    int progressMax = 10000;
    int percentage = (int) (viewer.getProgress(state) * progressMax);
    boolean indeterminate = percentage <= 0;
    return newProgressNotificationBuilder(state)
        .setContentTitle(viewer.getContentTitle(state))
        .setContentText(viewer.getContentText(state))
        .setProgress(progressMax, percentage, indeterminate)
        .setContentInfo(viewer.getContentInfo(state))
        .build();
  }

  private Notification.Builder newProgressNotificationBuilder(TaskState state) {
    TaskStateViewer viewer = getViewer(state);
    return new Notification.Builder(context)
        .setPriority(PRIORITY_LOW)
        .setSmallIcon(viewer.getSmallIcon())
        /*
         * Set when to a fixed value to prevent flickering on update when there
         * are multiple notifications being displayed/updated.
         */
        .setWhen(state.time().time())
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            context.getString(android.R.string.cancel),
            newCancelPendingIntent(context, state.task().id()));
  }

  private Notification newFailureNotification(TaskState.Failed state) {
    Intent intent = getFailureIntent(state);
    PendingIntent pending = getActivity(context, state.task().id(), intent,
        FLAG_UPDATE_CURRENT);
    return new Notification.Builder(context)
        .setSmallIcon(android.R.drawable.stat_notify_error)
        .setContentTitle(getTitle(intent))
        .setContentIntent(pending)
        .setAutoCancel(true)
        .build();
  }

  @VisibleForTesting Intent getFailureIntent(TaskState.Failed state) {
    TaskStateViewer viewer = getViewer(state);
    Collection<l.files.operations.Failure> failures = state.failures();
    ArrayList<FailureMessage> messages = new ArrayList<>(failures.size());
    for (l.files.operations.Failure failure : failures) {
      messages.add(FailureMessage.create(
          failure.path(), failure.cause().getMessage()));
    }
    String title = viewer.getContentTitle(state);
    return FailuresActivity.newIntent(context, title, messages);
  }
}
