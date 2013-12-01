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
import android.os.Handler;
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
  private Handler handler;

  @Override public void onCreate() {
    super.onCreate();
    tasks = new SparseArray<>();
    notificationManager = getNotificationManager(this);
    receiver = new CancellationReceiver();
    handler = new Handler();
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

  private static IntentFilter newCancellationIntentFilter(int id) {
    return new IntentFilter(newCancellationIntentAction(id));
  }

  /**
   * When there are multiple notifications, their {@link PendingIntent} cannot
   * share the same action, each must have a unique action for each notification
   * cancellation action to work correctly independently.
   */
  private static String newCancellationIntentAction(int id) {
    return ACTION_CANCEL + "/" + id;
  }

  private final class CancellationReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
      int id = intent.getIntExtra(EXTRA_ID, -1);
      AsyncTask<?, ?, ?> task = tasks.get(id);
      if (task != null) {
        task.cancel(true);
      }
    }
  }

  static abstract class Task<Params, Progress, Result>
      extends AsyncTask<Params, Progress, Result>
      implements Cancellable {

    private static final long PROGRESS_UPDATE_DELAY_MILLIS = 1000;

    private final int id;
    private final ProgressService service;
    private long lastProgressTime;

    Task(int id, ProgressService service) {
      this.id = id;
      this.service = service;
    }

    private Notification newNotification(Progress progress) {
      return newNotification(
          getNotificationContentTitle(progress),
          getNotificationContentText(progress),
          getNotificationProgressPercentage(progress));
    }

    private Notification newNotification(String title, String text) {
      return newNotification(title, text, 0);
    }

    private Notification newNotification(String title, String text, float progressPercentage) {
      return new Notification.Builder(service)
          .setContentTitle(title)
          .setContentText(text)
          .setSmallIcon(getNotificationSmallIcon())
          /*
           * Set when to a fixed value to prevent flickering on update when there
           * are multiple notifications being displayed/updated, since the when
           * value is not displayed, so it can be any fixed value.
           */
          .setWhen(id)
          .setShowWhen(false)
          .setOnlyAlertOnce(true)
          .setOngoing(true)
          .setProgress(100, (int) (progressPercentage * 100), progressPercentage == 0)
          .addAction(
              0,
              service.getString(android.R.string.cancel),
              newCancellationPendingIntent(id))
          .build();
    }

    private PendingIntent newCancellationPendingIntent(int id) {
      String action = newCancellationIntentAction(id);
      Intent intent = new Intent(action).putExtra(EXTRA_ID, id);
      return getBroadcast(service, id, intent, FLAG_UPDATE_CURRENT);
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
      if (!isCancelled()) {
        String title = getNotificationContentTitle();
        String text = service.getString(R.string.waiting);
        service.notificationManager.notify(id, newNotification(title, text));
      }
    }

    @SuppressWarnings("unchecked")
    @Override protected final Result doInBackground(Params... params) {
      try {
        return doTask();
      } catch (RuntimeException e) {
        service.handler.post(new Runnable() {
          @Override public void run() {
            onDone();
          }
        });
        throw e;
      }
    }

    @Override protected void onProgressUpdate(Progress[] values) {
      if (!isCancelled()) {
        service.notificationManager.notify(id, newNotification(values[0]));
      }
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

    protected abstract Result doTask();

    /**
     * Gets the ID of the notification icon to show.
     *
     * @see Notification.Builder#setSmallIcon(int)
     */
    protected abstract int getNotificationSmallIcon();

    /**
     * Gets the content title for the notification to be shown before any
     * progress has been made.
     */
    protected abstract String getNotificationContentTitle();

    /**
     * Gets the content title for the notification to be shown.
     *
     * @see AsyncTask#publishProgress(Object[])
     * @see Notification.Builder#setContentTitle(CharSequence)
     */
    protected String getNotificationContentTitle(Progress value) {
      return getNotificationContentTitle();
    }

    /**
     * Gets the content text for the notification to be shown.
     *
     * @see AsyncTask#publishProgress(Object[])
     * @see Notification.Builder#setContentText(CharSequence)
     */
    protected String getNotificationContentText(Progress value) {
      return null;
    }

    /**
     * Gets the current progress in percentage. Return 0 to indicate progress is
     * indeterminate.
     *
     * @see Notification.Builder#setProgress(int, int, boolean)
     */
    protected float getNotificationProgressPercentage(Progress value) {
      return 0;
    }
  }
}
