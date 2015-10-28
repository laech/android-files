package l.files.ui.preview;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import l.files.fs.File;
import l.files.fs.Stat;

import static android.content.Context.ACTIVITY_SERVICE;

final class ThumbnailMemCache extends MemCache<Bitmap> {

    private final LruCache<String, Snapshot<Bitmap>> delegate;

    ThumbnailMemCache(Context context, float appMemoryPercentageToUseForCache) {
        this(calculateSize(context, appMemoryPercentageToUseForCache));
    }

    ThumbnailMemCache(int size) {
        delegate = new LruCache<String, Snapshot<Bitmap>>(size) {
            @Override
            protected int sizeOf(String key, Snapshot<Bitmap> value) {
                return value.get().getByteCount();
            }
        };
    }

    private static int calculateSize(Context context, float appMemoryPercentageToUseForCache) {
        if (appMemoryPercentageToUseForCache <= 0 ||
                appMemoryPercentageToUseForCache >= 1) {
            throw new IllegalArgumentException();
        }

        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        int megabytes = manager.getMemoryClass();
        int bytes = megabytes * 1024 * 1024;
        return (int) (bytes * appMemoryPercentageToUseForCache);
    }

    @Override
    String key(File res, Stat stat, Rect constraint) {
        return res.scheme()
                + "_" + res.path()
                + "_" + constraint.width()
                + "_" + constraint.height();
    }

    @Override
    LruCache<String, Snapshot<Bitmap>> delegate() {
        return delegate;
    }

    void clear() {
        delegate().evictAll();
    }
}
