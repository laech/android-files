package l.files.operations;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;

import com.google.common.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import l.files.logging.Logger;
import l.files.operations.info.TaskInfo;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getBroadcast;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.Executors.newFixedThreadPool;

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

    private static final Executor executor = newFixedThreadPool(5);

    private Map<Integer, AsyncTask<?, ?, ?>> tasks;
    private CancellationReceiver cancellationReceiver;

    public static void delete(Context context, String... paths) {
        context.startService(
                new Intent(context, OperationService.class)
                        .setAction(ACTION_DELETE)
                        .putStringArrayListExtra(EXTRA_PATHS, newArrayList(paths))
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tasks = new HashMap<>();
        cancellationReceiver = new CancellationReceiver();
        registerCancellationReceiver();
        Events.get().register(this);
    }

    @Subscribe
    public void on(TaskInfo task) {
        if (task.getTaskStatus() == TaskInfo.TaskStatus.FINISHED) {
            tasks.remove(task.getTaskId());
            if (tasks.isEmpty()) {
                stopSelf();
            }
        }
    }

    private void registerCancellationReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_CANCEL);
        registerReceiver(cancellationReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        unregisterReceiver(cancellationReceiver);
        Events.get().unregister(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        if (intent != null) {
            Intent data = new Intent(intent);
            data.putExtra(EXTRA_TASK_ID, startId);

            @SuppressWarnings("unchecked")
            AsyncTask<Object, ?, ?> task = (AsyncTask<Object, ?, ?>) newTask(data, startId);
            tasks.put(startId, task);
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
        @Override
        public void onReceive(Context context, Intent intent) {
            int startId = intent.getIntExtra(EXTRA_TASK_ID, -1);
            AsyncTask<?, ?, ?> task = tasks.get(startId);
            if (task != null) {
                task.cancel(true);
            }
        }
    }
}
