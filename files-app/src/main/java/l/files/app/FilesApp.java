package l.files.app;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.StrictMode;
import android.util.LruCache;

import com.squareup.otto.Bus;

import l.files.R;
import l.files.common.event.Events;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static l.files.BuildConfig.DEBUG;
import static l.files.app.Preferences.newAnalyticsListener;

public final class FilesApp extends Application {

  public static Bus getBus(Fragment fragment) {
    return getBus(fragment.getActivity());
  }

  public static Bus getBus(Context context) {
    return getApp(context).bus;
  }

  public static LruCache<Object, Bitmap> getBitmapCache(Context context) {
    return getApp(context).bitmapCache;
  }

  public static Uri getFilesProviderAuthority(Context context) {
    return getApp(context).filesProviderAuthority;
  }

  public static Uri getFilesProviderAuthority(Fragment fragment) {
    return getFilesProviderAuthority(fragment.getActivity());
  }

  private static FilesApp getApp(Context context) {
    return (FilesApp) context.getApplicationContext();
  }

  private static FilesApp getApp(Fragment fragment) {
    return getApp(fragment.getActivity());
  }

  // TODO remove bus from here
  private Bus bus;
  private LruCache<Object, Bitmap> bitmapCache;
  private Uri filesProviderAuthority;

  @Override public void onCreate() {
    super.onCreate();

    bus = Events.bus();
    bitmapCache = createBitmapCache();
    filesProviderAuthority = Uri.parse("content://" +
        getString(R.string.files_provider_authority));

    Preferences.register(this, newAnalyticsListener(this));

    if (DEBUG) {
      StrictMode.enableDefaults();
    }
  }

  private LruCache<Object, Bitmap> createBitmapCache() {
    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    int megabytes = manager.getMemoryClass();
    int bytes = megabytes * 1024 * 1024;
    int size = bytes / 3;
    return new LruCache<Object, Bitmap>(size) {
      @Override protected int sizeOf(Object key, Bitmap value) {
        return value.getByteCount();
      }
    };
  }

  @Override public void onLowMemory() {
    super.onLowMemory();
    if (DEBUG) {
      makeText(this, "onLowMemory", LENGTH_SHORT).show();
    }
  }

  @Override public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    if (DEBUG) {
      makeText(this, "onTrimMemory: " + level, LENGTH_SHORT).show();
    }
    if (level >= TRIM_MEMORY_RUNNING_CRITICAL) {
      bitmapCache.evictAll();
    }
  }
}
