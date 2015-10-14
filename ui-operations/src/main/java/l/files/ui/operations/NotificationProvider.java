package l.files.ui.operations;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import l.files.operations.Clock;
import l.files.operations.OperationService.TaskListener;
import l.files.operations.TaskKind;
import l.files.operations.TaskNotFound;
import l.files.operations.TaskState;
import l.files.operations.TaskState.Failed;
import l.files.operations.TaskState.Pending;
import l.files.operations.TaskState.Running;
import l.files.operations.TaskState.Success;

import static android.app.Notification.PRIORITY_LOW;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.content.Context.NOTIFICATION_SERVICE;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static l.files.operations.OperationService.newCancelPendingIntent;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.TaskKind.DELETE;
import static l.files.operations.TaskKind.MOVE;
import static l.files.ui.base.fs.IOExceptions.message;
import static l.files.ui.operations.FailuresActivity.getTitle;

final class NotificationProvider implements TaskListener {

    private final Context context;
    private final NotificationManager manager;
    private final Map<TaskKind, ProgressViewer> viewers;

    public NotificationProvider(Context context, Clock clock) {
        this(context, clock, (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE));
    }

    public NotificationProvider(
            Context context, Clock clock, NotificationManager manager) {
        this.context = requireNonNull(context, "context");
        this.manager = requireNonNull(manager, "manager");
        this.viewers = buildViewers(context, clock);
    }

    private Map<TaskKind, ProgressViewer> buildViewers(Context context, Clock clock) {
        Map<TaskKind, ProgressViewer> viewers = new HashMap<>();
        viewers.put(MOVE, new MoveViewer(context, clock));
        viewers.put(COPY, new CopyViewer(context, clock));
        viewers.put(DELETE, new DeleteViewer(context, clock));
        return unmodifiableMap(viewers);
    }

    @Override
    public void onUpdate(TaskState state) {

        if (state instanceof Pending) {
            onEvent((Pending) state);

        } else if (state instanceof Running) {
            onEvent((Running) state);

        } else if (state instanceof Failed) {
            onEvent((Failed) state);

        } else if (state instanceof Success) {
            onEvent((Success) state);
        }

    }

    private void onEvent(Pending state) {
        manager.notify(state.getTask().getId(), newIndeterminateNotification(state));
    }

    private void onEvent(Running state) {
        manager.notify(state.getTask().getId(), newProgressNotification(state));
    }

    private void onEvent(Failed state) {
        manager.cancel(state.getTask().getId());
        if (!state.getFailures().isEmpty()) {
            // This is the last notification we will display for this task, and it
            // needs to stay until the user dismissed it, can't use the task ID as
            // the notification as when the service finishes, it will bring down the
            // startForeground notification with it.
            int id = Integer.MAX_VALUE - state.getTask().getId();
            manager.notify(id, newFailureNotification(state));
        }
        // If no file failures in collection, then failure is caused by some other
        // errors, let other process handle that error, remove the notification
    }

    private void onEvent(Success state) {
        manager.cancel(state.getTask().getId());
    }

    @Override
    public void onNotFound(TaskNotFound notFound) {
        manager.cancel(notFound.getTaskId());
    }

    private TaskStateViewer getViewer(TaskState state) {
        TaskStateViewer viewer = viewers.get(state.getTask().getKind());
        if (viewer == null) {
            throw new AssertionError(state);
        }
        return viewer;
    }

    private Notification newIndeterminateNotification(Pending state) {
        String title = getViewer(state).getContentTitle(state);
        return newIndeterminateNotification(state, title);
    }

    private Notification newIndeterminateNotification(TaskState s, String title) {
        return newProgressNotificationBuilder(s)
                .setContentTitle(title)
                .setProgress(1, 0, true)
                .build();
    }

    private Notification newProgressNotification(Running state) {
        TaskStateViewer viewer = getViewer(state);
        if (state.getItems().isDone() || state.getBytes().isDone()) {
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
                .setSmallIcon(viewer.getSmallIcon(context))
        /*
         * Set when to a fixed value to prevent flickering on update when there
         * are multiple notifications being displayed/updated.
         */
                .setWhen(state.getTime().getTime())
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .addAction(
                        R.drawable.ic_cancel_black_24dp,
                        context.getString(android.R.string.cancel),
                        newCancelPendingIntent(context, state.getTask().getId()));
    }

    private Notification newFailureNotification(Failed state) {
        Intent intent = getFailureIntent(state);
        PendingIntent pending = getActivity(
                context, state.getTask().getId(), intent, FLAG_UPDATE_CURRENT);
        return new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(getTitle(intent))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build();
    }

    public Intent getFailureIntent(Failed state) {
        TaskStateViewer viewer = getViewer(state);
        Collection<l.files.operations.Failure> failures = state.getFailures();
        ArrayList<FailureMessage> messages = new ArrayList<>(failures.size());
        for (l.files.operations.Failure failure : failures) {
            messages.add(FailureMessage.create(
                    failure.file(), message(failure.cause())));
        }
        String title = viewer.getContentTitle(state);
        return FailuresActivity.newIntent(context, title, messages);
    }
}
