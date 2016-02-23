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
import java.util.concurrent.ExecutorService;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.operations.Task.Callback;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getService;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.operations.OperationService.FileAction.COPY;
import static l.files.operations.OperationService.FileAction.DELETE;
import static l.files.operations.OperationService.FileAction.MOVE;

/**
 * Base class for services that perform operations on files.
 * <p>
 * Progress will be posted to the event bus, and at a controlled rate to avoid
 * listeners from being flooded by messages.
 */
public final class OperationService extends Service {

    static final String ACTION_CANCEL = "l.files.operations.CANCEL";
    static final String EXTRA_TASK_ID = "task_id";
    static final String EXTRA_SOURCE_DIRECTORY = "source_directory";
    static final String EXTRA_FILES = "source_files";
    static final String EXTRA_DESTINATION_DIRECTORY = "destination_directory";

    private static final ExecutorService executor = newFixedThreadPool(5);

    public static Intent newDeleteIntent(
            Context context,
            Path directory,
            Collection<? extends Name> files) {

        return new Intent(context, OperationService.class)
                .setAction(DELETE.action())
                .putExtra(EXTRA_SOURCE_DIRECTORY, directory)
                .putParcelableArrayListExtra(EXTRA_FILES, new ArrayList<>(files));
    }

    public static Intent newCopyIntent(
            Context context,
            Path sourceDirectory,
            Collection<? extends Name> sourceFiles,
            Path destinationDirectory) {

        return newPasteIntent(
                COPY.action(),
                context,
                sourceDirectory,
                sourceFiles,
                destinationDirectory);
    }

    public static Intent newMoveIntent(
            Context context,
            Path sourceDirectory,
            Collection<? extends Name> sourceFiles,
            Path destinationDirectory) {

        return newPasteIntent(
                MOVE.action(),
                context,
                sourceDirectory,
                sourceFiles,
                destinationDirectory);
    }

    private static Intent newPasteIntent(
            String action,
            Context context,
            Path sourceDirectory,
            Collection<? extends Name> sourceFiles,
            Path destinationDirectory) {

        return new Intent(context, OperationService.class)
                .setAction(action)
                .putExtra(EXTRA_DESTINATION_DIRECTORY, destinationDirectory)
                .putExtra(EXTRA_SOURCE_DIRECTORY, sourceDirectory)
                .putParcelableArrayListExtra(EXTRA_FILES, new ArrayList<>(sourceFiles));
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

    TaskListener listener;
    boolean foreground = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tasks = new HashMap<>();
        handler = new Handler(Looper.getMainLooper());
        if (listener == null) {
            listener = findListener();
        }
    }

    private TaskListener findListener() {
        try {
            String className = getString(R.string.l_files_operations_listeners);
            return (TaskListener) Class.forName(className).newInstance();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        if (foreground) {
            startForeground(startId, new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_storage_white_24dp)
                    .build());
        }

        Task task = newTask(data, startId, handler, new Callback() {
            @Override
            public void onUpdate(TaskState state) {
                if (state.isFinished()) {
                    tasks.remove(state.task().id());
                    if (tasks.isEmpty()) {
                        stopSelf();
                    }
                }
                listener.onUpdate(OperationService.this, state);
            }
        });
        tasks.put(startId, task.executeOnExecutor(executor));
    }

    private Task newTask(Intent intent, int id, Handler handler, Callback callback) {
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
            listener.onNotFound(this, TaskNotFound.create(startId));
        }
        if (tasks.isEmpty()) {
            stopSelf();
        }
    }

    enum FileAction {

        DELETE("l.files.operations.DELETE") {
            @Override
            Task newTask(Intent intent, int id, Handler handler, Callback callback) {
                List<Name> sourceFiles = intent.getParcelableArrayListExtra(EXTRA_FILES);
                Path sourceDirectory = intent.getParcelableExtra(EXTRA_SOURCE_DIRECTORY);
                return new DeleteTask(
                        id,
                        Clock.system(),
                        callback,
                        handler,
                        sourceDirectory,
                        sourceFiles);
            }
        },

        COPY("l.files.operations.COPY") {
            @Override
            Task newTask(Intent intent, int id, Handler handler, Callback callback) {
                List<Name> sourceFiles = intent.getParcelableArrayListExtra(EXTRA_FILES);
                Path sourceDirectory = intent.getParcelableExtra(EXTRA_SOURCE_DIRECTORY);
                Path destinationDirectory = intent.getParcelableExtra(EXTRA_DESTINATION_DIRECTORY);
                return new CopyTask(
                        id,
                        Clock.system(),
                        callback,
                        handler,
                        sourceDirectory,
                        sourceFiles,
                        destinationDirectory);
            }
        },

        MOVE("l.files.operations.MOVE") {
            @Override
            Task newTask(Intent intent, int id, Handler handler, Callback callback) {
                List<Name> sourceFiles = intent.getParcelableArrayListExtra(EXTRA_FILES);
                Path sourceDirectory = intent.getParcelableExtra(EXTRA_SOURCE_DIRECTORY);
                Path destinationDirectory = intent.getParcelableExtra(EXTRA_DESTINATION_DIRECTORY);
                return new MoveTask(
                        id,
                        Clock.system(),
                        callback,
                        handler,
                        sourceDirectory,
                        sourceFiles,
                        destinationDirectory);
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

        void onUpdate(Context context, TaskState state);

        void onNotFound(Context context, TaskNotFound notFound);

    }

}
