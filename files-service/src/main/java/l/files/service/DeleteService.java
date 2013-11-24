package l.files.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.SparseArray;

import java.io.File;
import java.text.NumberFormat;
import java.util.Set;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getBroadcast;
import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static com.google.common.collect.Sets.newHashSet;
import static l.files.common.app.SystemServices.getNotificationManager;
import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.common.io.Files.toFiles;

public final class DeleteService extends Service {

  private static final String ACTION_CANCEL = "l.files.intent.action.CANCEL";
  private static final String EXTRA_START_ID = "l.files.intent.extra.START_ID";
  private static final String EXTRA_FILE_PATHS = "l.files.intent.extra.FILE_PATHS";

  private SparseArray<Preparation> tasks;
  private NotificationManager notificationManager;
  private BroadcastReceiver receiver;
  private NumberFormat numberFormat;

  public static void delete(Context context, Set<File> files) {
    context.startService(new Intent(context, DeleteService.class)
        .putExtra(EXTRA_FILE_PATHS, toAbsolutePaths(files)));
  }

  @Override public void onCreate() {
    super.onCreate();
    numberFormat = NumberFormat.getIntegerInstance();
    tasks = new SparseArray<>();
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

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      registerReceiver(receiver, newCancellationIntentFilter(startId));
      intent.putExtra(EXTRA_START_ID, startId);

      Preparation task = new Preparation(startId);
      tasks.put(startId, task);
      task.executeOnExecutor(THREAD_POOL_EXECUTOR, getFiles(intent));
    }
    return START_STICKY;
  }

  private File[] getFiles(Intent intent) {
    return toFiles(newHashSet(intent.getStringArrayExtra(EXTRA_FILE_PATHS)));
  }

  private Notification.Builder newCancellableNotification(
      int startId, boolean showProgressBar, String title, String text) {

    Notification.Builder builder = new Notification.Builder(this)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_stat_notify_delete)
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
            0,
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
   * When there are multiple notifications, their {@link
   * android.app.PendingIntent} cannot share the same action, each must have a
   * unique action for each notification cancellation action to work correctly
   * independently.
   */
  private String newCancellationIntentAction(int startId) {
    return ACTION_CANCEL + "/" + startId;
  }

  private final class CancellationReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
      int startId = intent.getIntExtra(EXTRA_START_ID, -1);
      Preparation task = tasks.get(startId);
      if (task != null) task.cancel(true);
    }
  }

  private final class Preparation extends FileCounter {
    Preparation(int id) {
      super(id);
    }

    @Override protected void onPreExecute() {
      startForeground(id(), newCancellableNotification(id(), false,
          getContentTitle(), getString(R.string.waiting)).build());
    }

    @Override protected void onProgressUpdate(Result... values) {
      Result result = values[0];
      Notification notification = newCancellableNotification(id(), true,
          getContentTitle(),
          getContentText(result.filesCount())).build();

      notificationManager.notify(id(), notification);
    }

    @Override protected void onPostExecute(Result result) {
      onDone();
    }

    @Override protected void onCancelled(Result result) {
      onDone();
    }

    private void onDone() {
      notificationManager.cancel(id());
      tasks.remove(id());
      if (tasks.size() == 0) stopSelf();
    }

    private String getContentTitle() {
      return getString(R.string.preparing_to_delete);
    }

    private String getContentText(int count) {
      return getString(R.string.preparing_to_delete_x_items,
          numberFormat.format(count));
    }
  }
}
