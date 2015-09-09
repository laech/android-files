package l.files.operations;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

import l.files.R;
import l.files.fs.File;
import l.files.operations.Task.Callback;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getService;
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
  static final String EXTRA_RESOURCES = "resources";
  static final String EXTRA_DESTINATION = "destination";

  private static final ExecutorService executor = newFixedThreadPool(5);
  private static final Set<TaskListener> listeners = new CopyOnWriteArraySet<>();

  public static void addListener(TaskListener listener) {
    listeners.add(listener);
  }

  public static void removeListener(TaskListener listener) {
    listeners.remove(listener);
  }

  public static void delete(
      Context context,
      Collection<? extends File> resources) {
    context.startService(
        new Intent(context, OperationService.class)
            .setAction(DELETE.action())
            .putParcelableArrayListExtra(EXTRA_RESOURCES, new ArrayList<>(resources))
    );
  }

  public static void copy(
      Context context,
      Collection<? extends File> sources,
      File destination) {
    paste(COPY.action(), context, sources, destination);
  }

  public static void move(
      Context context,
      Collection<? extends File> sources,
      File destination) {
    paste(MOVE.action(), context, sources, destination);
  }

  private static void paste(
      String action,
      Context context,
      Collection<? extends File> sources,
      File destination) {
    context.startService(
        new Intent(context, OperationService.class)
            .setAction(action)
            .putExtra(EXTRA_DESTINATION, destination)
            .putParcelableArrayListExtra(EXTRA_RESOURCES, new ArrayList<>(sources))
    );
  }

  public static PendingIntent newCancelPendingIntent(Context context, int id) {
    Intent intent = newCancelIntent(context, id);
    return getService(context, id, intent, FLAG_UPDATE_CURRENT);
  }

  public static Intent newCancelIntent(Context context, int id) {
    return new Intent(context, OperationService.class)
        .setAction(ACTION_CANCEL).putExtra(EXTRA_TASK_ID, id);
  }

  private Handler handler;
  private Map<Integer, AsyncTask<?, ?, ?>> tasks;

  @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();
    tasks = new HashMap<>();
    handler = new Handler(Looper.getMainLooper());
  }

  @Override public void onDestroy() {
    super.onDestroy();
    stopForeground(true);
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
        .setSmallIcon(R.mipmap.ic_launcher)
        .build());

    Task task = newTask(data, startId, handler, new Callback() {
      @Override public void onUpdate(TaskState state) {
        if (state.isFinished()) {
          tasks.remove(state.getTask().getId());
          if (tasks.isEmpty()) {
            stopSelf();
          }
        }
        for (TaskListener listener : listeners) {
          listener.onUpdate(state);
        }
      }
    });
    tasks.put(startId, task.executeOnExecutor(executor));
  }

  private Task newTask(Intent intent, int id, Handler handler, Callback callback) {
    intent.setExtrasClassLoader(getClassLoader());
    return FileAction
        .fromIntent(intent.getAction())
        .newTask(intent, id, handler, callback);
  }

  void cancelTask(Intent intent) {
    int startId = intent.getIntExtra(EXTRA_TASK_ID, -1);
    AsyncTask<?, ?, ?> task = tasks.remove(startId);
    if (task != null) {
      task.cancel(true);
    } else {
      TaskNotFound notFound = TaskNotFound.create(startId);
      for (TaskListener listener : listeners) {
        listener.onNotFound(notFound);
      }
    }
    if (tasks.isEmpty()) {
      stopSelf();
    }
  }

  enum FileAction {

    DELETE("l.files.operations.DELETE") {
      @Override
      Task newTask(Intent intent, int id, Handler handler, Callback callback) {
        List<File> files = intent.getParcelableArrayListExtra(EXTRA_RESOURCES);
        return new DeleteTask(id, Clock.system(), callback, handler, files);
      }
    },

    COPY("l.files.operations.COPY") {
      @Override
      Task newTask(Intent intent, int id, Handler handler, Callback callback) {
        List<File> sources = intent.getParcelableArrayListExtra(EXTRA_RESOURCES);
        File destination = intent.getParcelableExtra(EXTRA_DESTINATION);
        return new CopyTask(id, Clock.system(), callback, handler, sources, destination);
      }
    },

    MOVE("l.files.operations.MOVE") {
      @Override
      Task newTask(Intent intent, int id, Handler handler, Callback callback) {
        List<File> sources = intent.getParcelableArrayListExtra(EXTRA_RESOURCES);
        File destination = intent.getParcelableExtra(EXTRA_DESTINATION);
        return new MoveTask(id, Clock.system(), callback, handler, sources, destination);
      }
    };

    private String action;

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

    abstract Task newTask(Intent intent, int id, Handler handler, Callback callback);
  }

  public interface TaskListener {

    void onUpdate(TaskState state);

    void onNotFound(TaskNotFound notFound);

  }

}
