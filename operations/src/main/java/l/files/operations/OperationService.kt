package l.files.operations

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import l.files.fs.Path
import java.util.*
import java.util.concurrent.Executors

class OperationService internal constructor(
  private val foreground: Boolean,
  listener: (Context) -> TaskListener
) : Service() {

  @Suppress("unused") // Used by Android
  constructor() : this(true, {
    Class.forName(it.getString(R.string.l_files_operations_listeners))
      .newInstance() as TaskListener
  })

  private val handler = Handler(Looper.getMainLooper())
  private val tasks = HashMap<Int, AsyncTask<*, *, *>>()
  private val listener: TaskListener by lazy { listener(this) }

  override fun onBind(intent: Intent) = null

  override fun onDestroy() {
    super.onDestroy()
    stopForeground(true)
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    if (ACTION_CANCEL == intent.action) {
      cancelTask(intent)
    } else {
      executeTask(intent, startId)
    }
    // Return START_NOT_STICKY because this service shouldn't be automatically
    // restarted, after the process died, especially if the cause of the crash
    // was programming error
    return START_NOT_STICKY
  }

  private fun executeTask(intent: Intent, startId: Int) {
    val data = Intent(intent)
    data.putExtra(EXTRA_TASK_ID, startId)

    // A dummy notification so that the service can use startForeground, making
    // it less likely to be destroy, the notification will be replaced with ones
    // from operations-ui
    if (foreground) {

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ensureNotificationChannelExists()
      }

      startForeground(
        startId,
        NotificationCompat.Builder(this, OPERATIONS_NOTIFICATION_CHANNEL_ID)
          .setSmallIcon(R.drawable.ic_storage_white_24dp)
          .build()
      )
    }

    val task = newTask(
      data,
      startId,
      handler,
      Task.Callback { state ->
        if (state.isFinished) {
          tasks.remove(state.task().id())
          if (tasks.isEmpty()) {
            stopSelf()
          }
        }
        listener.onUpdate(this@OperationService, state)
      }
    )
    tasks[startId] = task.executeOnExecutor(executor)
  }

  private fun newTask(
    intent: Intent,
    id: Int,
    handler: Handler,
    callback: Task.Callback
  ): Task =
    fileActionFromIntent(intent.action!!).newTask(intent, id, handler, callback)

  private fun cancelTask(intent: Intent) {
    val startId = intent.getIntExtra(EXTRA_TASK_ID, -1)
    tasks.remove(startId)
      ?.cancel(true)
      ?: listener.onNotFound(
        this,
        TaskNotFound.create(startId)
      )
    if (tasks.isEmpty()) {
      stopSelf()
    }
  }
}

@RequiresApi(api = Build.VERSION_CODES.O)
private fun Context.ensureNotificationChannelExists() {
  val manager = getSystemService<NotificationManager>()!!
  var channel = manager.getNotificationChannel(
    OPERATIONS_NOTIFICATION_CHANNEL_ID
  )
  if (channel == null) {
    channel = NotificationChannel(
      OPERATIONS_NOTIFICATION_CHANNEL_ID,
      getString(R.string.operations_notification_channel_name),
      IMPORTANCE_DEFAULT
    )
    manager.createNotificationChannel(channel)
  }
}

internal enum class FileAction(val action: String) {

  DELETE("l.files.operations.DELETE") {
    override fun newTask(
      intent: Intent,
      id: Int,
      handler: Handler,
      callback: Task.Callback
    ) = DeleteTask(
      id,
      Clock.system(),
      callback,
      handler,
      intent.getParcelableArrayListExtra(EXTRA_PATHS)
    )
  },

  COPY("l.files.operations.COPY") {
    override fun newTask(
      intent: Intent,
      id: Int,
      handler: Handler,
      callback: Task.Callback
    ) = CopyTask(
      id,
      Clock.system(),
      callback,
      handler,
      intent.getParcelableArrayListExtra(EXTRA_PATHS),
      intent.getParcelableExtra(EXTRA_DESTINATION)
    )
  },

  MOVE("l.files.operations.MOVE") {
    override fun newTask(
      intent: Intent,
      id: Int,
      handler: Handler,
      callback: Task.Callback
    ) = MoveTask(
      id,
      Clock.system(),
      callback,
      handler,
      intent.getParcelableArrayListExtra(EXTRA_PATHS),
      intent.getParcelableExtra(EXTRA_DESTINATION)
    )
  };

  abstract fun newTask(
    intent: Intent,
    id: Int,
    handler: Handler,
    callback: Task.Callback
  ): Task
}

interface TaskListener {
  fun onUpdate(context: Context, state: TaskState)
  fun onNotFound(context: Context, notFound: TaskNotFound)
}

internal fun fileActionFromIntent(action: String): FileAction =
  FileAction.values().first { it.action == action }

const val OPERATIONS_NOTIFICATION_CHANNEL_ID = "l.files.operations"
const val ACTION_CANCEL = "l.files.operations.CANCEL"
const val EXTRA_TASK_ID = "task_id"

private const val EXTRA_PATHS = "paths"
private const val EXTRA_DESTINATION = "destination"

private val executor = Executors.newFixedThreadPool(5)

fun newDeleteIntent(
  context: Context,
  files: Collection<Path>
): Intent = Intent(context, OperationService::class.java)
  .setAction(FileAction.DELETE.action)
  .putParcelableArrayListExtra(EXTRA_PATHS, ArrayList(files))

fun newCopyIntent(
  context: Context,
  sources: Collection<Path>,
  destination: Path
): Intent = newPasteIntent(
  FileAction.COPY.action,
  context,
  sources,
  destination
)

fun newMoveIntent(
  context: Context,
  sources: Collection<Path>,
  destination: Path
): Intent = newPasteIntent(
  FileAction.MOVE.action,
  context,
  sources,
  destination
)

private fun newPasteIntent(
  action: String,
  context: Context,
  sources: Collection<Path>,
  destination: Path
): Intent = Intent(context, OperationService::class.java)
  .setAction(action)
  .putExtra(EXTRA_DESTINATION, destination)
  .putParcelableArrayListExtra(EXTRA_PATHS, ArrayList(sources))

fun newCancelPendingIntent(context: Context, id: Int): PendingIntent =
  PendingIntent.getService(
    context,
    id,
    newCancelIntent(context, id),
    FLAG_UPDATE_CURRENT
  )

fun newCancelIntent(context: Context, id: Int): Intent =
  Intent(context, OperationService::class.java)
    .setAction(ACTION_CANCEL)
    .putExtra(EXTRA_TASK_ID, id)
