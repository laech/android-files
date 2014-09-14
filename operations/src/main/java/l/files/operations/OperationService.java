package l.files.operations;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.greenrobot.event.EventBus;
import l.files.eventbus.Subscribe;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getBroadcast;
import static com.google.common.collect.Lists.newArrayList;
import static de.greenrobot.event.ThreadMode.MainThread;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.operations.OperationService.FileAction.COPY;
import static l.files.operations.OperationService.FileAction.DELETE;
import static l.files.operations.OperationService.FileAction.MOVE;

/**
 * Base class for services that perform operations on files.
 * <p/>
 * Progress will be posted to the event bus, and at a controlled rate to avoid
 * listeners from being flooded by messages.
 */
public final class OperationService extends Service {

  private static final String ACTION_CANCEL = "l.files.operations.CANCEL";
  private static final String EXTRA_TASK_ID = "task_id";
  private static final String EXTRA_PATHS = "paths";
  private static final String EXTRA_DST_PATH = "dstPath";

  private static final ExecutorService executor = newFixedThreadPool(5);

  private EventBus bus;
  private Handler handler;
  private Map<Integer, Future<?>> tasks;
  private CancellationReceiver cancellationReceiver;

  public static void delete(Context context, String... paths) {
    context.startService(
        new Intent(context, OperationService.class)
            .setAction(DELETE.action())
            .putStringArrayListExtra(EXTRA_PATHS, newArrayList(paths))
    );
  }

  public static void copy(Context context, Iterable<String> srcPaths, String dstPath) {
    paste(COPY.action(), context, srcPaths, dstPath);
  }

  public static void move(Context context, Iterable<String> srcPaths, String dstPath) {
    paste(MOVE.action(), context, srcPaths, dstPath);
  }

  private static void paste(String action, Context context, Iterable<String> srcPaths, String dstPath) {
    context.startService(
        new Intent(context, OperationService.class)
            .setAction(action)
            .putExtra(EXTRA_DST_PATH, dstPath)
            .putStringArrayListExtra(EXTRA_PATHS, newArrayList(srcPaths))
    );
  }

  /**
   * Creates an intent to be broadcast for cancelling a running task.
   */
  public static PendingIntent newCancelIntent(Context context, int taskId) {
    // Don't set class name as that causes pending intent to not work
    Intent intent = new Intent(ACTION_CANCEL).putExtra(EXTRA_TASK_ID, taskId);
    return getBroadcast(context, taskId, intent, FLAG_UPDATE_CURRENT);
  }

  @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();
    bus = Events.get();
    tasks = new HashMap<>();
    handler = new Handler();
    cancellationReceiver = new CancellationReceiver();
    registerCancellationReceiver();
    bus.register(this);
  }

  @Subscribe(MainThread)
  public void onEventMainThread(TaskState state) {
    if (state.isFinished()) {
      tasks.remove(state.task().id());
      if (tasks.isEmpty()) {
        stopSelf();
      }
    }
  }

  private void registerCancellationReceiver() {
    IntentFilter filter = new IntentFilter(ACTION_CANCEL);
    registerReceiver(cancellationReceiver, filter);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    stopForeground(true);
    unregisterReceiver(cancellationReceiver);
    bus.unregister(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, final int startId) {
    if (intent != null) {
      Intent data = new Intent(intent);
      data.putExtra(EXTRA_TASK_ID, startId);

      Task task = newTask(data, startId, bus, handler);
      tasks.put(startId, task.execute(executor));
    }
    return START_STICKY;
  }

  private Task newTask(
      Intent intent, int startId, EventBus bus, Handler handler) {
    return FileAction.fromIntent(intent.getAction())
        .newTask(intent, startId, bus, handler);
  }

  private final class CancellationReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
      int startId = intent.getIntExtra(EXTRA_TASK_ID, -1);
      Future<?> task = tasks.get(startId);
      if (task != null) {
        task.cancel(true);
      }
    }
  }

  static enum FileAction {

    DELETE("l.files.operations.DELETE") {
      @Override
      Task newTask(Intent intent, int startId, EventBus bus, Handler handler) {
        List<String> paths = intent.getStringArrayListExtra(EXTRA_PATHS);
        return new DeleteTask(startId, Clock.system(), bus, handler, paths);
      }
    },

    COPY("l.files.operations.COPY") {
      @Override
      Task newTask(Intent intent, int startId, EventBus bus, Handler handler) {
        List<String> srcPaths = intent.getStringArrayListExtra(EXTRA_PATHS);
        String dstPath = intent.getStringExtra(EXTRA_DST_PATH);
        return new CopyTask(startId, Clock.system(), bus, handler, srcPaths, dstPath);
      }
    },

    MOVE("l.files.operations.MOVE") {
      @Override
      Task newTask(Intent intent, int startId, EventBus bus, Handler handler) {
        List<String> srcPaths = intent.getStringArrayListExtra(EXTRA_PATHS);
        String dstPath = intent.getStringExtra(EXTRA_DST_PATH);
        return new MoveTask(startId, Clock.system(), bus, handler, srcPaths, dstPath);
      }
    };

    private final String action;

    FileAction(String action) {
      this.action = action;
    }

    public String action() {
      return action;
    }

    public static FileAction fromIntent(String action) {
      for (FileAction value : values()) {
        if (value.action.equals(action)) {
          return value;
        }
      }
      throw new IllegalArgumentException("Unknown action: " + action);
    }

    abstract Task newTask(
        Intent intent, int startId, EventBus bus, Handler handler);
  }
}
