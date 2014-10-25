package l.files.ui.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.google.common.base.Optional;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Intent.ACTION_BUG_REPORT;
import static android.os.IBinder.FIRST_CALL_TRANSACTION;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Allows user to send feedback using the system feedback framework.
 */
public final class GoogleFeedback implements ServiceConnection {

  private static final String TAG = GoogleFeedback.class.getSimpleName();

  private final Activity activity;

  private GoogleFeedback(Activity activity) {
    this.activity = checkNotNull(activity, "activity");
  }

  /**
   * Invoke to allow the user to send a feedback for this app.
   *
   * @param activity the activity invoking this, a screenshot will be taken
   */
  public static void send(Activity activity) {
    activity.bindService(
        new Intent(ACTION_BUG_REPORT),
        new GoogleFeedback(activity),
        BIND_AUTO_CREATE);
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    try {
      Optional<Bitmap> screenshot = Screenshots.take(activity);
      Parcel parcel = Parcel.obtain();
      if (screenshot.isPresent()) {
        screenshot.get().writeToParcel(parcel, 0);
        screenshot.get().recycle();
      }
      service.transact(FIRST_CALL_TRANSACTION, parcel, null, 0);

    } catch (RemoteException e) {
      Log.e(TAG, e.getMessage(), e); // TODO track
    } finally {
      activity.unbindService(this);
    }
  }

  @Override public void onServiceDisconnected(ComponentName name) {}
}
