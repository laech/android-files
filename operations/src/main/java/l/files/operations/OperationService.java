package l.files.operations;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import static android.app.PendingIntent.getService;
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

  static final String ACTION_CANCEL = "l.files.operations.CANCEL";
  static final String EXTRA_TASK_ID = "task_id";
  static final String EXTRA_PATHS = "paths";
  static final String EXTRA_DST_PATH = "dstPath";

  private static final ExecutorService executor = newFixedThreadPool(5);

  EventBus bus;
  private Handler handler;
  private Map<Integer, Future<?>> tasks;

  public static void delete(Context context, String... paths) {
    context.startService(
        new Intent(context, OperationService.class)
            .setAction(DELETE.action())
            .putStringArrayListExtra(EXTRA_PATHS, newArrayList(paths))
    );
  }

  public static void copy(
      Context context, Iterable<String> srcPaths, String dstPath) {
    paste(COPY.action(), context, srcPaths, dstPath);
  }

  public static void move(
      Context context, Iterable<String> srcPaths, String dstPath) {
    paste(MOVE.action(), context, srcPaths, dstPath);
  }

  private static void paste(String action, Context context,
                            Iterable<String> srcPaths, String dstPath) {
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
    return getService(context, taskId, intent, FLAG_UPDATE_CURRENT);
  }

  @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();
    bus = Events.get();
    tasks = new HashMap<>();
    handler = new Handler();
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

  @Override public void onDestroy() {
    super.onDestroy();
    stopForeground(true);
    bus.unregister(this);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      if (ACTION_CANCEL.equals(intent.getAction())) {
        cancelTask(intent);
      } else {
        executeTask(intent, startId);
      }
    }
    return START_STICKY;
  }

  private void executeTask(Intent intent, int startId) {
    Intent data = new Intent(intent);
    data.putExtra(EXTRA_TASK_ID, startId);

    startForeground(startId, new Notification.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher).build());
    Task task = newTask(data, startId, bus, handler);
    tasks.put(startId, task.execute(executor));
  }

  private Task newTask(Intent intent, int id, EventBus bus, Handler handler) {
    return FileAction
        .fromIntent(intent.getAction())
        .newTask(intent, id, bus, handler);
  }

  void cancelTask(Intent intent) {
    int startId = intent.getIntExtra(EXTRA_TASK_ID, -1);
    Future<?> task = tasks.remove(startId);
    if (task != null) {
      task.cancel(true);
    } else {
      bus.post(TaskNotFound.create(startId));
    }
    if (tasks.isEmpty()) {
      stopSelf();
    }
  }

  static enum FileAction {

    DELETE("l.files.operations.DELETE") {
      @Override
      Task newTask(Intent intent, int id, EventBus bus, Handler handler) {
        List<String> paths = intent.getStringArrayListExtra(EXTRA_PATHS);
        return new DeleteTask(id, Clock.system(), bus, handler, paths);
      }
    },

    COPY("l.files.operations.COPY") {
      @Override
      Task newTask(Intent intent, int id, EventBus bus, Handler handler) {
        List<String> srcPaths = intent.getStringArrayListExtra(EXTRA_PATHS);
        String dstPath = intent.getStringExtra(EXTRA_DST_PATH);
        return new CopyTask(id, Clock.system(), bus, handler, srcPaths, dstPath);
      }
    },

    MOVE("l.files.operations.MOVE") {
      @Override
      Task newTask(Intent intent, int id, EventBus bus, Handler handler) {
        List<String> srcPaths = intent.getStringArrayListExtra(EXTRA_PATHS);
        String dstPath = intent.getStringExtra(EXTRA_DST_PATH);
        return new MoveTask(id, Clock.system(), bus, handler, srcPaths, dstPath);
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

    abstract Task newTask(Intent intent, int id, EventBus bus, Handler handler);
  }
}
