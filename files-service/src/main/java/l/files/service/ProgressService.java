package l.files.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.SparseArray;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getBroadcast;
import static l.files.common.app.SystemServices.getNotificationManager;

abstract class ProgressService extends Service {

  private static final String ACTION_CANCEL = "l.files.intent.action.CANCEL";
  private static final String EXTRA_ID = "l.files.intent.extra.ID";
  private static int idSeq;

  private SparseArray<Task<?, ?, ?>> tasks;
  private NotificationManager notificationManager;
  private BroadcastReceiver receiver;

  @Override public void onCreate() {
    super.onCreate();
    tasks = new SparseArray<>();
    notificationManager = getNotificationManager(this);
    receiver = new CancellationReceiver();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(receiver);
    stopForeground(true);
  }

  @SuppressWarnings("unchecked")
  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      int id = ++idSeq;
      registerReceiver(receiver, newCancellationIntentFilter(id));
      intent.putExtra(EXTRA_ID, id);

      Task<?, ?, ?> task = newTask(intent, id);
      tasks.put(id, task);
      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    return START_STICKY;
  }

  protected abstract Task<?, ?, ?> newTask(Intent intent, int id);

  @Override public IBinder onBind(Intent intent) {
    return null;
  }

  private Notification.Builder newCancellableNotification(
      int id, boolean showProgressBar, String title, String text) {

    Notification.Builder builder = new Notification.Builder(this)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_stat_notify_delete)
        /*
         * Set when to a fixed value to prevent flickering on update when there
         * are multiple notifications being displayed/updated, since the when
         * value is not displayed, so it can be any fixed value.
         */
        .setWhen(id)
        .setShowWhen(false)
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .addAction(
            0,
            getString(android.R.string.cancel),
            newCancellationPendingIntent(id));

    if (showProgressBar) builder.setProgress(0, 0, true);

    return builder;
  }

  private PendingIntent newCancellationPendingIntent(int id) {
    String action = newCancellationIntentAction(id);
    Intent intent = new Intent(action).putExtra(EXTRA_ID, id);
    return getBroadcast(this, id, intent, FLAG_UPDATE_CURRENT);
  }

  private IntentFilter newCancellationIntentFilter(int id) {
    return new IntentFilter(newCancellationIntentAction(id));
  }

  /**
   * When there are multiple notifications, their {@link PendingIntent} cannot
   * share the same action, each must have a unique action for each notification
   * cancellation action to work correctly independently.
   */
  private String newCancellationIntentAction(int id) {
    return ACTION_CANCEL + "/" + id;
  }

  private final class CancellationReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
      AsyncTask<?, ?, ?> task = tasks.get(intent.getIntExtra(EXTRA_ID, -1));
      if (task != null) task.cancel(true);
    }
  }

  static abstract class Task<Params, Progress, Result>
      extends AsyncTask<Params, Progress, Result> {

    private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

    private final int id;
    private final ProgressService service;
    private long lastProgressTime;

    Task(int id, ProgressService service) {
      this.id = id;
      this.service = service;
    }

    /**
     * This method is intended to be called during {@link
     * #doInBackground(Object[])} to check whether {@link
     * #publishProgress(Object[])} should be executed to avoid excessive amount
     * of notifications to be sent, if true the current time will be recorded.
     */
    protected boolean setAndGetUpdateProgress() {
      long time = elapsedTime();
      if (time - lastProgressTime >= PROGRESS_UPDATE_DELAY_MILLIS) {
        lastProgressTime = time;
        return true;
      }
      return false;
    }

    @Override protected void onPreExecute() {
      String title = getContentTitle();
      String summary = service.getString(R.string.waiting);
      service.notificationManager.notify(id,
          service.newCancellableNotification(id, false, title, summary).build());
    }

    @Override protected void onProgressUpdate(Progress[] values) {
      String title = getContentTitle(values);
      String summary = getContentText(values);
      service.notificationManager.notify(id,
          service.newCancellableNotification(id, true, title, summary).build());
    }

    private long elapsedTime() {
      return SystemClock.elapsedRealtime();
    }

    @Override protected void onPostExecute(Result result) {
      onDone();
    }

    @Override protected void onCancelled(Result result) {
      onDone();
    }

    private void onDone() {
      service.notificationManager.cancel(id);
      service.tasks.remove(id);
      if (service.tasks.size() == 0) {
        service.stopSelf();
      }
    }

    /**
     * Gets the content title for the notification to be shown before any
     * progress has been made.
     */
    protected abstract String getContentTitle();

    /**
     * Gets the content title for the notification to be shown.
     *
     * @see AsyncTask#publishProgress(Object[])
     * @see Notification.Builder#setContentTitle(CharSequence)
     */
    protected String getContentTitle(Progress[] values) {
      return getContentTitle();
    }

    /**
     * Gets the content text for the notification to be shown.
     *
     * @see AsyncTask#publishProgress(Object[])
     * @see Notification.Builder#setContentText(CharSequence)
     */
    protected String getContentText(Progress[] values) {
      return null;
    }
  }
}
