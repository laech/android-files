package l.files.app.io;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import com.google.common.collect.ImmutableSet;
import l.files.R;

import java.io.File;
import java.text.NumberFormat;
import java.util.Set;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getBroadcast;
import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static l.files.app.io.PasteInspection.Progress;
import static l.files.common.app.SystemServices.getNotificationManager;
import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.common.io.Files.toFiles;

/**
 * Service to prepare source files/directories for move/copy, reports any
 * conflicts, e.g. files that already exists.
 */
public final class PreparationService extends Service {

  private static final String ACTION_CANCEL = "l.files.intent.action.CANCEL";
  private static final String EXTRA_START_ID = "l.files.intent.extra.START_ID";
  private static final String EXTRA_SOURCES = "l.files.intent.extra.SOURCES";
  private static final String EXTRA_DEST_DIR = "l.files.intent.extra.DEST_DIR";
  private static final String EXTRA_TYPE = "l.files.intent.action.TYPE";

  private SparseArray<Task> operations;
  private NotificationManager notificationManager;
  private BroadcastReceiver receiver;

  public static void prepareMove(
      Context context, Set<File> sources, File destDir) {
    start(context, sources, destDir, NotificationType.MOVE);
  }

  public static void prepareCopy(
      Context context, Set<File> sources, File destDir) {
    start(context, sources, destDir, NotificationType.COPY);
  }

  private static void start(
      Context context, Set<File> sources, File destDir, NotificationType type) {
    context.startService(new Intent(context, PreparationService.class)
        .putExtra(EXTRA_SOURCES, toAbsolutePaths(sources))
        .putExtra(EXTRA_DEST_DIR, destDir.getAbsolutePath())
        .putExtra(EXTRA_TYPE, type));
  }

  @Override public void onCreate() {
    super.onCreate();
    operations = new SparseArray<Task>();
    notificationManager = getNotificationManager(this);
    receiver = new CancellationReceiver();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(receiver);
    stopForeground(true);
  }

  @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public int onStartCommand(Intent it, int flags, int startId) {
    if (it != null) {
      registerReceiver(receiver, newCancellationIntentFilter(startId));
      it.putExtra(EXTRA_START_ID, startId);

      Task task = new Task(startId, sources(it), destDir(it), type(it));
      operations.put(startId, task);
      task.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }
    return START_STICKY;
  }

  private NotificationType type(Intent intent) {
    return (NotificationType) intent.getSerializableExtra(EXTRA_TYPE);
  }

  private File destDir(Intent intent) {
    String destDirPath = intent.getStringExtra(EXTRA_DEST_DIR);
    return new File(destDirPath);
  }

  private Set<File> sources(Intent intent) {
    String[] srcPaths = intent.getStringArrayExtra(EXTRA_SOURCES);
    return ImmutableSet.copyOf(toFiles(srcPaths));
  }

  private Notification.Builder newCancellableNotification(
      int startId,
      boolean showProgressBar,
      CharSequence title,
      CharSequence text) {

    Notification.Builder builder = new Notification.Builder(this)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_launcher)
        /*
         * Set when to a fixed value to prevent flickering on update when there
         * are multiple notifications being displayed/updated, since the when
         * value is not displayed, so it can be any fixed value.
         */
        .setWhen(startId)
        .setShowWhen(false)
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            getString(android.R.string.cancel),
            newCancellationPendingIntent(startId));

    if (showProgressBar) builder.setProgress(0, 0, true);

    return builder;
  }

  private PendingIntent newCancellationPendingIntent(int startId) {
    final Intent intent = new Intent(newCancellationIntentAction(startId))
        .putExtra(EXTRA_START_ID, startId);
    return getBroadcast(this, startId, intent, FLAG_UPDATE_CURRENT);
  }

  private IntentFilter newCancellationIntentFilter(int startId) {
    return new IntentFilter(newCancellationIntentAction(startId));
  }

  /**
   * When there are multiple notifications, their {@link PendingIntent} cannot
   * share the same action, each must have a unique action for each notification
   * cancellation action to work correctly independently.
   */
  private String newCancellationIntentAction(int startId) {
    return ACTION_CANCEL + "/" + startId;
  }

  private final class CancellationReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
      int startId = intent.getIntExtra(EXTRA_START_ID, -1);
      Task task = operations.get(startId);
      if (task != null) task.cancel(true);
    }
  }

  private static enum NotificationType {
    MOVE(R.string.preparing_to_move_to_x, R.string.preparing_to_move_x_items),
    COPY(R.string.preparing_to_copy_to_x, R.string.preparing_to_copy_x_items);

    private static NumberFormat fmt = NumberFormat.getIntegerInstance();

    private final int contentTitleResId;
    private final int contentTextResId;

    NotificationType(int contentTitleResId, int contentTextResId) {
      this.contentTitleResId = contentTitleResId;
      this.contentTextResId = contentTextResId;
    }

    CharSequence getContentTitle(Resources res, File destDir) {
      return res.getString(contentTitleResId, destDir.getName());
    }

    CharSequence getContentText(Resources res, Progress progress) {
      if (progress == null) {
        return res.getString(R.string.waiting);
      }
      return res.getString(contentTextResId, fmt.format(progress.count()));
    }
  }

  private final class Task extends PasteInspection {
    private final NotificationType type;

    Task(int id, Set<File> sources, File destDir, NotificationType type) {
      super(id, sources, destDir);
      this.type = type;
    }

    @Override protected void onPreExecute() {
      startForeground(id(), newCancellableNotification(id(), false,
          getContentTitle(), getContentText(null)).build());
    }

    @Override protected void onProgressUpdate(Progress... values) {
      notificationManager.notify(id(), newCancellableNotification(id(), true,
          getContentTitle(), getContentText(values[0])).build());
    }

    private CharSequence getContentText(Progress progress) {
      return type.getContentText(getResources(), progress);
    }

    private CharSequence getContentTitle() {
      return type.getContentTitle(getResources(), destination());
    }

    @Override protected void onPostExecute(Result result) {
      Log.d("PreparationService", "conflicts:\n" + result.conflicts().toString().replaceAll(",", "\n"));
      Log.d("PreparationService", "errors:\n" + result.errors().toString().replaceAll(",", "\n"));
      onDone();
    }

    @Override protected void onCancelled(Result result) {
      onDone();
    }

    private void onDone() {
      notificationManager.cancel(id());
      operations.remove(id());
      if (operations.size() == 0) stopSelf();
    }
  }
}
