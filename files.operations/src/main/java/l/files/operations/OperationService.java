package l.files.operations;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import l.files.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.operations.Progress.STATUS_FINISHED;
import static l.files.operations.Progress.getTaskId;
import static l.files.operations.Progress.getTaskStatus;

/**
 * Base class for services that perform operations on files.
 * <p/>
 * Progress will be posted to the event bus, and at a controlled rate to avoid
 * listeners from being flooded by messages.
 */
public final class OperationService extends Service {

  private static final Logger logger = Logger.get(OperationService.class);

  private static final String ACTION_CANCEL = "l.files.operations.CANCEL";
  private static final String EXTRA_TASK_ID = "task_id";

  private static final String ACTION_DELETE = "l.files.operations.DELETE";
  private static final String EXTRA_PATHS = "paths";
  private static final String EXTRA_ROOT_PATH = "root_path";

  private static final Executor executor = newFixedThreadPool(5);

  private Map<Integer, AsyncTask<?, ?, ?>> tasks;
  private CancellationReceiver cancellationReceiver;
  private TaskCleanupReceiver taskCleanupReceiver;

  /**
   * Starts this service to delete the given files.
   *
   * @param rootPath the common root path of all the paths to delete
   * @param paths the paths to be deleted
   */
  public static void delete(Context context, String rootPath, String... paths) {
    context.startService(
        new Intent(context, OperationService.class)
            .setAction(ACTION_DELETE)
            .putExtra(EXTRA_ROOT_PATH, checkNotNull(rootPath, "rootPath"))
            .putStringArrayListExtra(EXTRA_PATHS, newArrayList(paths))
    );
  }

  /**
   * Creates an intent to be broadcast for cancelling a running task.
   */
  public static Intent newCancelIntent(int taskId) {
    // Don't set class name as that causes pending intent to not work
    return new Intent(ACTION_CANCEL).putExtra(EXTRA_TASK_ID, taskId);
  }

  @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();
    tasks = new HashMap<>();
    cancellationReceiver = new CancellationReceiver();
    taskCleanupReceiver = new TaskCleanupReceiver();
    registerCancellationReceiver();
    registerTaskCleanupReceiver();
  }

  private void registerTaskCleanupReceiver() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(Progress.Delete.ACTION);
    registerReceiver(
        taskCleanupReceiver, filter, Permissions.SEND_PROGRESS, null);
  }

  private void registerCancellationReceiver() {
    IntentFilter filter = new IntentFilter(ACTION_CANCEL);
    registerReceiver(cancellationReceiver, filter);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    stopForeground(true);
    unregisterReceiver(cancellationReceiver);
    unregisterReceiver(taskCleanupReceiver);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, final int startId) {
    if (intent != null) {
      Intent data = new Intent(intent);
      data.putExtra(EXTRA_TASK_ID, startId);

      AsyncTask<?, ?, ?> task = newTask(data, startId);
      tasks.put(startId, task);
      //noinspection unchecked
      task.executeOnExecutor(executor);
    }
    return START_STICKY;
  }

  private AsyncTask<?, ?, ?> newTask(Intent intent, int startId) {
    switch (intent.getAction()) {
      case ACTION_DELETE: {
        String rootPath = intent.getStringExtra(EXTRA_ROOT_PATH);
        List<String> paths = intent.getStringArrayListExtra(EXTRA_PATHS);
        logger.debug("delete %s", paths);
        return new DeleteTask(this, startId, rootPath, paths);
      }
      default:
        throw new IllegalArgumentException(intent.getAction());
    }
  }

  private final class CancellationReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
      int startId = intent.getIntExtra(EXTRA_TASK_ID, -1);
      AsyncTask<?, ?, ?> task = tasks.get(startId);
      if (task != null) {
        task.cancel(true);
      }
    }
  }

  private final class TaskCleanupReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
      if (getTaskStatus(intent) == STATUS_FINISHED) {
        tasks.remove(getTaskId(intent));
        if (tasks.isEmpty()) {
          stopSelf();
        }
      }
    }
  }

}
