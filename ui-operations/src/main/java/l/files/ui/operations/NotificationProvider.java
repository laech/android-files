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
import static l.files.operations.OperationService.newCancelPendingIntent;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.TaskKind.DELETE;
import static l.files.operations.TaskKind.MOVE;
import static l.files.ui.base.fs.IOExceptions.message;
import static l.files.ui.operations.FailuresActivity.getTitle;

public final class NotificationProvider implements TaskListener {

    private final Map<TaskKind, ProgressViewer> viewers;

    private static NotificationManager manager(Context context) {
        return (NotificationManager) context
                .getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
    }

    @SuppressWarnings("unused")
    public NotificationProvider() {
        this(Clock.system());
    }

    NotificationProvider(Clock clock) {
        this.viewers = buildViewers(clock);
    }

    private Map<TaskKind, ProgressViewer> buildViewers(Clock clock) {
        Map<TaskKind, ProgressViewer> viewers = new HashMap<>();
        viewers.put(MOVE, new MoveViewer(clock));
        viewers.put(COPY, new CopyViewer(clock));
        viewers.put(DELETE, new DeleteViewer(clock));
        return unmodifiableMap(viewers);
    }

    @Override
    public void onUpdate(Context context, TaskState state) {

        if (state instanceof Pending) {
            onEvent(context, (Pending) state);

        } else if (state instanceof Running) {
            onEvent(context, (Running) state);

        } else if (state instanceof Failed) {
            onEvent(context, (Failed) state);

        } else if (state instanceof Success) {
            onEvent(context, (Success) state);
        }

    }

    private void onEvent(Context context, Pending state) {
        manager(context).notify(
                state.task().id(),
                newIndeterminateNotification(context, state)
        );
    }

    private void onEvent(Context context, Running state) {
        manager(context).notify(
                state.task().id(),
                newProgressNotification(context, state)
        );
    }

    private void onEvent(Context context, Failed state) {
        manager(context).cancel(state.task().id());
        if (!state.failures().isEmpty()) {
            // This is the last notification we will display for this task, and it
            // needs to stay until the user dismissed it, can't use the task ID as
            // the notification as when the service finishes, it will bring down the
            // startForeground notification with it.
            int id = Integer.MAX_VALUE - state.task().id();
            manager(context).notify(id, newFailureNotification(context, state));
        }
        // If no file failures in collection, then failure is caused by some other
        // errors, let other process handle that error, remove the notification
    }

    private void onEvent(Context context, Success state) {
        manager(context).cancel(state.task().id());
    }

    @Override
    public void onNotFound(Context context, TaskNotFound notFound) {
        manager(context).cancel(notFound.id());
    }

    private TaskStateViewer getViewer(TaskState state) {
        TaskStateViewer viewer = viewers.get(state.task().kind());
        if (viewer == null) {
            throw new AssertionError(state);
        }
        return viewer;
    }

    private Notification newIndeterminateNotification(Context context, Pending state) {
        String title = getViewer(state).getContentTitle(context, state);
        return newIndeterminateNotification(context, state, title);
    }

    private Notification newIndeterminateNotification(
            Context context, TaskState state, String title) {

        return newProgressNotificationBuilder(context, state)
                .setContentTitle(title)
                .setProgress(1, 0, true)
                .build();
    }

    private Notification newProgressNotification(Context context, Running state) {
        TaskStateViewer viewer = getViewer(state);
        String title = viewer.getContentTitle(context, state);
        if (state.items().isDone() || state.bytes().isDone()) {
            return newIndeterminateNotification(context, state, title);
        }
        int progressMax = 10000;
        int percentage = (int) (viewer.getProgress(context, state) * progressMax);
        boolean indeterminate = percentage <= 0;
        return newProgressNotificationBuilder(context, state)
                .setContentTitle(title)
                .setContentText(viewer.getContentText(context, state))
                .setProgress(progressMax, percentage, indeterminate)
                .setContentInfo(viewer.getContentInfo(context, state))
                .build();
    }

    private Notification.Builder newProgressNotificationBuilder(
            Context context, TaskState state) {

        TaskStateViewer viewer = getViewer(state);
        return new Notification.Builder(context)
                .setPriority(PRIORITY_LOW)
                .setSmallIcon(viewer.getSmallIcon(context))
                /*
                 * Set when to a fixed value to prevent flickering on update when there
                 * are multiple notifications being displayed/updated.
                 */
                .setWhen(state.time().time())
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .addAction(
                        R.drawable.ic_cancel_black_24dp,
                        context.getString(android.R.string.cancel),
                        newCancelPendingIntent(context, state.task().id()));
    }

    private Notification newFailureNotification(Context context, Failed state) {
        Intent intent = getFailureIntent(context, state);
        PendingIntent pending = getActivity(
                context, state.task().id(), intent, FLAG_UPDATE_CURRENT);
        return new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(getTitle(intent))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build();
    }

    Intent getFailureIntent(Context context, Failed state) {
        TaskStateViewer viewer = getViewer(state);
        Collection<l.files.operations.Failure> failures = state.failures();
        ArrayList<FailureMessage> messages = new ArrayList<>(failures.size());
        for (l.files.operations.Failure failure : failures) {
            messages.add(FailureMessage.create(
                    failure.file(), message(failure.cause())));
        }
        String title = viewer.getContentTitle(context, state);
        return FailuresActivity.newIntent(context, title, messages);
    }
}
