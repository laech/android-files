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

import l.files.operations.Clock;
import l.files.operations.TaskKind;
import l.files.operations.TaskNotFound;
import l.files.operations.TaskState;

import static android.app.Notification.PRIORITY_LOW;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.content.Context.NOTIFICATION_SERVICE;
import static java.util.Objects.requireNonNull;
import static l.files.operations.OperationService.newCancelPendingIntent;
import static l.files.operations.TaskKind.COPY;
import static l.files.operations.TaskKind.DELETE;
import static l.files.operations.TaskKind.MOVE;
import static l.files.operations.ui.FailuresActivity.getTitle;

final class NotificationProvider
{

    private final Context context;
    private final NotificationManager manager;
    private final Map<TaskKind, ProgressViewer> viewers;

    public NotificationProvider(
            final Context context,
            final Clock clock)
    {
        this(context, clock, (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE));
    }

    public NotificationProvider(
            final Context context,
            final Clock clock,
            final NotificationManager manager)
    {
        this.context = requireNonNull(context, "context");
        this.manager = requireNonNull(manager, "manager");
        this.viewers = ImmutableMap.of(
                MOVE, new MoveViewer(context, clock),
                COPY, new CopyViewer(context, clock),
                DELETE, new DeleteViewer(context, clock)
        );
    }

    public void onEvent(final TaskState.Pending state)
    {
        manager.notify(state.getTask().getId(), newIndeterminateNotification(state));
    }

    public void onEvent(final TaskState.Running state)
    {
        manager.notify(state.getTask().getId(), newProgressNotification(state));
    }

    public void onEvent(final TaskState.Failed state)
    {
        manager.cancel(state.getTask().getId());
        if (!state.getFailures().isEmpty())
        {
            // This is the last notification we will display for this task, and it
            // needs to stay until the user dismissed it, can't use the task ID as
            // the notification as when the service finishes, it will bring down the
            // startForeground notification with it.
            final int id = Integer.MAX_VALUE - state.getTask().getId();
            manager.notify(id, newFailureNotification(state));
        }
        // If no file failures in collection, then failure is caused by some other
        // errors, let other process handle that error, remove the notification
    }

    public void onEvent(final TaskState.Success state)
    {
        manager.cancel(state.getTask().getId());
    }

    public void onEvent(final TaskNotFound event)
    {
        manager.cancel(event.getTaskId());
    }

    private TaskStateViewer getViewer(final TaskState state)
    {
        final TaskStateViewer viewer = viewers.get(state.getTask().getKind());
        if (viewer == null)
        {
            throw new AssertionError(state);
        }
        return viewer;
    }

    private Notification newIndeterminateNotification(final TaskState.Pending state)
    {
        final String title = getViewer(state).getContentTitle(state);
        return newIndeterminateNotification(state, title);
    }

    private Notification newIndeterminateNotification(final TaskState s, final String title)
    {
        return newProgressNotificationBuilder(s)
                .setContentTitle(title)
                .setProgress(1, 0, true)
                .build();
    }

    private Notification newProgressNotification(final TaskState.Running state)
    {
        final TaskStateViewer viewer = getViewer(state);
        if (state.getItems().isDone() || state.getBytes().isDone())
        {
            return newIndeterminateNotification(state, viewer.getContentTitle(state));
        }
        final int progressMax = 10000;
        final int percentage = (int) (viewer.getProgress(state) * progressMax);
        final boolean indeterminate = percentage <= 0;
        return newProgressNotificationBuilder(state)
                .setContentTitle(viewer.getContentTitle(state))
                .setContentText(viewer.getContentText(state))
                .setProgress(progressMax, percentage, indeterminate)
                .setContentInfo(viewer.getContentInfo(state))
                .build();
    }

    private Notification.Builder newProgressNotificationBuilder(final TaskState state)
    {
        final TaskStateViewer viewer = getViewer(state);
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
                        android.R.drawable.ic_menu_close_clear_cancel,
                        context.getString(android.R.string.cancel),
                        newCancelPendingIntent(context, state.getTask().getId()));
    }

    private Notification newFailureNotification(final TaskState.Failed state)
    {
        final Intent intent = getFailureIntent(state);
        final PendingIntent pending = getActivity(context, state.getTask().getId(), intent,
                FLAG_UPDATE_CURRENT);
        return new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(getTitle(intent))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build();
    }

    @VisibleForTesting
    public Intent getFailureIntent(final TaskState.Failed state)
    {
        final TaskStateViewer viewer = getViewer(state);
        final Collection<l.files.operations.Failure> failures = state.getFailures();
        final ArrayList<FailureMessage> messages = new ArrayList<>(failures.size());
        for (final l.files.operations.Failure failure : failures)
        {
            messages.add(FailureMessage.create(
                    failure.getResource(), failure.getCause().getMessage() + ""));
        }
        final String title = viewer.getContentTitle(state);
        return FailuresActivity.newIntent(context, title, messages);
    }
}
