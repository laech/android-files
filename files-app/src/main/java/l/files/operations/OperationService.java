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
import l.files.R;
import l.files.eventbus.Subscribe;
import l.files.fs.Path;

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

  public static void delete(Context context, Iterable<? extends Path> paths) {
    context.startService(
        new Intent(context, OperationService.class)
            .setAction(DELETE.action())
            .putParcelableArrayListExtra(EXTRA_PATHS, newArrayList(paths))
    );
  }

  public static void copy(Context context, Iterable<? extends Path> src, Path dst) {
    paste(COPY.action(), context, src, dst);
  }

  public static void move(Context context, Iterable<? extends Path> src, Path dst) {
    paste(MOVE.action(), context, src, dst);
  }

  private static void paste(String action, Context context,
                            Iterable<? extends Path> src, Path dst) {
    context.startService(
        new Intent(context, OperationService.class)
            .setAction(action)
            .putExtra(EXTRA_DST_PATH, dst)
            .putParcelableArrayListExtra(EXTRA_PATHS, newArrayList(src))
    );
  }

  public static PendingIntent newCancelPendingIntent(Context context, int id) {
    Intent intent = newCancelIntent(context, id);
    return getService(context, id, intent, FLAG_UPDATE_CURRENT);
  }

  static Intent newCancelIntent(Context context, int id) {
    return new Intent(context, OperationService.class)
        .setAction(ACTION_CANCEL).putExtra(EXTRA_TASK_ID, id);
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
    if (state.getIsFinished()) {
      tasks.remove(state.getTask().getId());
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
    if (ACTION_CANCEL.equals(intent.getAction())) {
      cancelTask(intent);
    } else {
      executeTask(intent, startId);
    }
    // Return START_NOT_STICKY because this service shouldn't be automatically
    // restarted, after the process died, especially if the cause of the crash
    // was programming error
    return START_NOT_STICKY;
  }

  private void executeTask(Intent intent, int startId) {
    Intent data = new Intent(intent);
    data.putExtra(EXTRA_TASK_ID, startId);

    // A dummy notification so that the service can use startForeground, making
    // it less likely to be destroy, the notification will be replaced with ones
    // from operations-ui
    startForeground(startId, new Notification.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .build());

    Task task = newTask(data, startId, bus, handler);
    tasks.put(startId, task.execute(executor));
  }

  private Task newTask(Intent intent, int id, EventBus bus, Handler handler) {
    intent.setExtrasClassLoader(getClassLoader());
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
      @Override Task newTask(Intent intent, int id, EventBus bus, Handler handler) {
        List<Path> paths = intent.getParcelableArrayListExtra(EXTRA_PATHS);
        return new DeleteTask(id, Clock.system(), bus, handler, paths);
      }
    },

    COPY("l.files.operations.COPY") {
      @Override Task newTask(Intent intent, int id, EventBus bus, Handler handler) {
        List<Path> sources = intent.getParcelableArrayListExtra(EXTRA_PATHS);
        Path destination = intent.getParcelableExtra(EXTRA_DST_PATH);
        return new CopyTask(id, Clock.system(), bus, handler, sources, destination);
      }
    },

    MOVE("l.files.operations.MOVE") {
      @Override Task newTask(Intent intent, int id, EventBus bus, Handler handler) {
        List<Path> sources = intent.getParcelableArrayListExtra(EXTRA_PATHS);
        Path destination = intent.getParcelableExtra(EXTRA_DST_PATH);
        return new MoveTask(id, Clock.system(), bus, handler, sources, destination);
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
