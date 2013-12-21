package l.files.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.LruCache;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.util.Set;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.google.common.collect.Sets.newHashSet;
import static l.files.BuildConfig.DEBUG;
import static l.files.app.Preferences.newAnalyticsListener;

public final class FilesApp extends Application {

  public static Bus getBus(Fragment fragment) {
    return getBus(fragment.getActivity());
  }

  public static Bus getBus(Context context) {
    return ((FilesApp) context.getApplicationContext()).bus;
  }

  public static LruCache<Object, Bitmap> getBitmapCache(Context context) {
    return ((FilesApp) context.getApplicationContext()).bitmapCache;
  }


  private Bus bus;
  private LruCache<Object, Bitmap> bitmapCache;

  @Override public void onCreate() {
    super.onCreate();

    bus = new Bus(ThreadEnforcer.MAIN) {

      // TODO
      private final Set<Object> objects = newHashSet();

      @Override
      public void register(Object object) {
        if (objects.add(object)) super.register(object);
      }

      @Override
      public void unregister(Object object) {
        if (objects.remove(object)) super.unregister(object);
      }

    };

    bitmapCache = createBitmapCache();

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
