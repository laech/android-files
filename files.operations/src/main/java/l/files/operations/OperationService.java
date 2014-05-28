package l.files.operations;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;

import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import l.files.logging.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.operations.Progress.State.FINISHED;

/**
 * Base class for services that perform operations on files.
 *
 * <p>Progress will be posted to the event bus, and at a controlled rate to
 * avoid listeners from being flooded by messages.
 */
public final class OperationService extends Service {

  private static final Logger logger = Logger.get(OperationService.class);

  private static final String ACTION_CANCEL = "l.files.operations.CANCEL";
  private static final String EXTRA_TASK_ID = "task_id";

  private static final String ACTION_DELETE = "l.files.operations.DELETE";
  private static final String EXTRA_PATHS = "paths";

  private static final Executor executor = createExecutor();

  private static ThreadPoolExecutor createExecutor() {
    int corePoolSize = 1;
    int maxPoolSize = 5;
    int keepAliveTimeSeconds = 1;
    TimeUnit unit = SECONDS;
    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    return new ThreadPoolExecutor(corePoolSize, maxPoolSize,
        keepAliveTimeSeconds, unit, workQueue);
  }

  private Map<Integer, AsyncTask<?, ?, ?>> tasks;
  private CancellationReceiver cancellationReceiver;

  /**
   * Starts this service to delete the given files.
   */
  public static void delete(Context context, String... paths) {
    context.startService(
        new Intent(context, OperationService.class)
            .setAction(ACTION_DELETE)
            .putStringArrayListExtra(EXTRA_PATHS, newArrayList(paths))
    );
  }

  @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();
    tasks = new HashMap<>();
    cancellationReceiver = new CancellationReceiver();
    registerReceiver(cancellationReceiver, new IntentFilter(ACTION_CANCEL));
    EventBus.get().register(this);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    stopForeground(true);
    unregisterReceiver(cancellationReceiver);
    EventBus.get().unregister(this);
  }

  @Subscribe public void on(Progress progress) {
    if (progress.state() == FINISHED) {
      tasks.remove(progress.taskId());
      if (tasks.isEmpty()) {
        stopSelf();
      }
    }
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
        List<String> paths = intent.getStringArrayListExtra(EXTRA_PATHS);
        logger.debug("delete %s", paths);
        return new DeleteTask(startId, paths);
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
}
