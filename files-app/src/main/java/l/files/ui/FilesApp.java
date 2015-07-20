package l.files.ui;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.util.LruCache;

import l.files.operations.ui.OperationsUi;
import l.files.ui.preview.Preview;

import static l.files.BuildConfig.DEBUG;

public final class FilesApp extends Application {

  public static LruCache<Object, Bitmap> getBitmapCache(Context context) {
    return getApp(context).bitmapCache;
  }

  private static FilesApp getApp(Context context) {
    return (FilesApp) context.getApplicationContext();
  }

  private LruCache<Object, Bitmap> bitmapCache;

  @Override public void onCreate() {
    super.onCreate();
    Preview.get(this).cleanupAsync();

    bitmapCache = createBitmapCache();

    new OperationsUi().init(this);

    if (DEBUG) {
      StrictMode.setThreadPolicy(
          new ThreadPolicy.Builder()
              .detectAll()
              .penaltyDialog()
              .penaltyLog()
              .build());

      StrictMode.setVmPolicy(
          new VmPolicy.Builder()
              .detectActivityLeaks()
              .detectLeakedClosableObjects()
              .detectLeakedRegistrationObjects()
              .detectLeakedSqlLiteObjects()
              .penaltyLog()
              .build());
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
