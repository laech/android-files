package l.files.app.menu;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.google.common.base.Optional;

import static android.os.IBinder.FIRST_CALL_TRANSACTION;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.app.Activities.takeScreenshotForFeedback;

final class FeedbackConnection implements ServiceConnection {

  private static final String TAG = FeedbackConnection.class.getSimpleName();

  private final Activity activity;

  public FeedbackConnection(Activity activity) {
    this.activity = checkNotNull(activity, "activity");
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    try {
      Optional<Bitmap> screenshot = takeScreenshotForFeedback(activity);
      Parcel parcel = Parcel.obtain();
      if (screenshot.isPresent()) {
        screenshot.get().writeToParcel(parcel, 0);
        screenshot.get().recycle();
      }
      service.transact(FIRST_CALL_TRANSACTION, parcel, null, 0);

    } catch (RemoteException e) {
      Log.e(TAG, e.getMessage(), e); // TODO
    } finally {
      activity.unbindService(this);
    }
  }

  @Override public void onServiceDisconnected(ComponentName name) {}
}
