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

import static l.files.BuildConfig.DEBUG;

public final class FilesApp extends Application
{
    public static LruCache<Object, Bitmap> getBitmapCache(final Context context)
    {
        return getApp(context).bitmapCache;
    }

    private static FilesApp getApp(final Context context)
    {
        return (FilesApp) context.getApplicationContext();
    }

    private LruCache<Object, Bitmap> bitmapCache;

    @Override
    public void onCreate()
    {
        super.onCreate();

        bitmapCache = createBitmapCache();

        new OperationsUi().init(this);

        if (DEBUG)
        {
            StrictMode.setThreadPolicy(
                    new ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyDialog()
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

    private LruCache<Object, Bitmap> createBitmapCache()
    {
        final ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        final int megabytes = manager.getMemoryClass();
        final int bytes = megabytes * 1024 * 1024;
        final int size = bytes / 3;
        return new LruCache<Object, Bitmap>(size)
        {
            @Override
            protected int sizeOf(final Object key, final Bitmap value)
            {
                return value.getByteCount();
            }
        };
    }

    @Override
    public void onTrimMemory(final int level)
    {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_RUNNING_CRITICAL)
        {
            bitmapCache.evictAll();
        }
    }
}
