package l.files.ui.operations

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import l.files.operations.*
import l.files.operations.TaskState.*
import l.files.ui.base.fs.IOExceptions
import java.util.*
import java.util.Collections.unmodifiableMap

class NotificationProvider private constructor(clock: Clock) :
  TaskListener {

  // Called by reflection
  constructor() : this(Clock.system())

  private val viewers =
    EnumMap<TaskKind, ProgressViewer>(TaskKind::class.java).also {
      it[TaskKind.MOVE] = MoveViewer(clock)
      it[TaskKind.COPY] = CopyViewer(clock)
      it[TaskKind.DELETE] = DeleteViewer(clock)
      unmodifiableMap(it)
    }

  private fun getViewer(state: TaskState): TaskStateViewer =
    viewers[state.task().kind()]
      ?: throw AssertionError(state)

  override fun onUpdate(context: Context, state: TaskState) {
    when (state) {
      is Pending -> onEvent(context, state)
      is Running -> onEvent(context, state)
      is Failed -> onEvent(context, state)
      is Success -> onEvent(context, state)
    }
  }

  private fun onEvent(context: Context, state: Pending) {
    context.getSystemService<NotificationManager>()!!.notify(
      state.task().id(),
      newIndeterminateNotification(context, state)
    )
  }

  private fun onEvent(context: Context, state: Running) {
    context.getSystemService<NotificationManager>()!!.notify(
      state.task().id(),
      newProgressNotification(context, state)
    )
  }

  private fun onEvent(context: Context, state: Failed) {
    val manager = context.getSystemService<NotificationManager>()!!
    manager.cancel(state.task().id())
    if (state.failures().isNotEmpty()) {
      // This is the last notification we will display for this task, and it
      // needs to stay until the user dismissed it, can't use the task ID as
      // the notification as when the service finishes, it will bring down the
      // startForeground notification with it.
      val id = Int.MAX_VALUE - state.task().id()
      manager.notify(id, newFailureNotification(context, state))
    }
    // If no file failures in collection, then failure is caused by some other
    // errors, let other process handle that error, remove the notification
  }

  private fun onEvent(context: Context, state: Success) {
    context.getSystemService<NotificationManager>()!!.cancel(state.task().id())
  }

  override fun onNotFound(context: Context, notFound: TaskNotFound) {
    context.getSystemService<NotificationManager>()!!.cancel(notFound.id())
  }

  private fun newIndeterminateNotification(
    context: Context,
    state: Pending
  ) = newIndeterminateNotification(
    context,
    state,
    getViewer(state).getContentTitlePending(context)
  )

  private fun newIndeterminateNotification(
    context: Context,
    state: TaskState,
    title: String
  ) = newProgressNotificationBuilder(context, state)
    .setContentTitle(title)
    .setProgress(1, 0, true)
    .build()

  private fun newProgressNotification(
    context: Context,
    state: Running
  ): Notification {
    val viewer = getViewer(state)
    val title = viewer.getContentTitleRunning(context, state)
    if (state.items().isDone || state.bytes().isDone) {
      return newIndeterminateNotification(context, state, title)
    }
    val progressMax = 10000
    val percentage = (viewer.getProgress(state) * progressMax).toInt()
    val indeterminate = percentage <= 0
    return newProgressNotificationBuilder(context, state)
      .setContentTitle(title)
      .setContentText(viewer.getContentText(context, state))
      .setProgress(progressMax, percentage, indeterminate)
      .setContentInfo(viewer.getContentInfo(context, state))
      .build()
  }

  private fun newProgressNotificationBuilder(
    context: Context,
    state: TaskState
  ) = NotificationCompat.Builder(context, OPERATIONS_NOTIFICATION_CHANNEL_ID)
    .setPriority(NotificationCompat.PRIORITY_LOW)
    .setSmallIcon(getViewer(state).getSmallIcon(context))
    /*
     * Set when to a fixed value to prevent flickering on update when there
     * are multiple notifications being displayed/updated.
     */
    .setWhen(state.time().time())
    .setOnlyAlertOnce(true)
    .setOngoing(true)
    .addAction(
      NotificationCompat.Action(
        R.drawable.ic_cancel_black_24dp,
        context.getString(android.R.string.cancel),
        newCancelPendingIntent(context, state.task().id())
      )
    )

  private fun newFailureNotification(
    context: Context,
    state: Failed
  ): Notification {
    val intent = getFailureIntent(context, state)
    val pending = PendingIntent.getActivity(
      context,
      state.task().id(),
      intent,
      FLAG_UPDATE_CURRENT
    )
    return NotificationCompat.Builder(
      context,
      OPERATIONS_NOTIFICATION_CHANNEL_ID
    )
      .setSmallIcon(android.R.drawable.stat_notify_error)
      .setContentTitle(FailuresActivity.getTitle(intent))
      .setContentIntent(pending)
      .setAutoCancel(true)
      .build()
  }

  fun getFailureIntent(context: Context, state: Failed): Intent =
    FailuresActivity.newIntent(
      context,
      getViewer(state).getContentTitleFailed(context, state),
      state.failures().map {
        FailureMessage.create(
          it.path(),
          IOExceptions.message(it.cause())
        )
      }
    )
}
