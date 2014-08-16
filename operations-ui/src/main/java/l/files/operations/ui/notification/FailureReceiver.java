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

import l.files.operations.info.CopyTaskInfo;
import l.files.operations.info.DeleteTaskInfo;
import l.files.operations.info.MoveTaskInfo;
import l.files.operations.info.TaskInfo;
import l.files.operations.ui.FailureMessage;
import l.files.operations.ui.FailuresActivity;
import l.files.operations.ui.R;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.content.Context.NOTIFICATION_SERVICE;
import static l.files.io.file.operations.FileOperation.Failure;
import static l.files.operations.info.TaskInfo.TaskStatus.FINISHED;
import static l.files.operations.ui.FailuresActivity.getTitle;

public class FailureReceiver {

  private final Context context;
  private final NotificationManager manager;

  FailureReceiver(Context context, NotificationManager manager) {
    this.context = context;
    this.manager = manager;
  }

  public static void register(EventBus bus, Context context) {
    NotificationManager m = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    register(bus, context, m);
  }

  public static void register(EventBus bus, Context context, NotificationManager manager) {
    FailureReceiver receiver = new FailureReceiver(context, manager);
    bus.register(receiver);
  }

  @Subscribe public void on(CopyTaskInfo value) {
    on(value, R.plurals.fail_to_copy);
  }

  @Subscribe public void on(MoveTaskInfo value) {
    on(value, R.plurals.fail_to_move);
  }

  @Subscribe public void on(DeleteTaskInfo value) {
    on(value, R.plurals.fail_to_delete);
  }

  @VisibleForTesting public void on(TaskInfo value, int pluralTitleId) {
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

