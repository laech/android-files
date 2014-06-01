package l.files.app;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.util.LruCache;

import com.squareup.otto.Bus;

import l.files.common.event.Events;
import l.files.operations.ui.OperationsUi;

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

  private static FilesApp getApp(Context context) {
    return (FilesApp) context.getApplicationContext();
  }

  // TODO remove bus from here
  private Bus bus;
  private LruCache<Object, Bitmap> bitmapCache;

  @Override public void onCreate() {
    super.onCreate();

    bus = Events.bus();
    bitmapCache = createBitmapCache();

    Preferences.register(this, newAnalyticsListener(this));

    OperationsUi.init(this);

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

  @Override public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    if (level >= TRIM_MEMORY_RUNNING_CRITICAL) {
      bitmapCache.evictAll();
    }
  }
}
