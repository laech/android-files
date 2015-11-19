package l.files.ui.preview;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import l.files.fs.File;
import l.files.fs.Stat;

import static android.content.Context.ACTIVITY_SERVICE;

final class ThumbnailMemCache extends MemCache<Bitmap> {

    private final LruCache<CharBuffer, Snapshot<Bitmap>> delegate;

    ThumbnailMemCache(Context context, float appMemoryPercentageToUseForCache) {
        this(calculateSize(context, appMemoryPercentageToUseForCache));
    }

    ThumbnailMemCache(int size) {
        delegate = new LruCache<CharBuffer, Snapshot<Bitmap>>(size) {
            @Override
            protected int sizeOf(CharBuffer key, Snapshot<Bitmap> value) {
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
    void key(CharBuffer key, File res, Stat stat, Rect constraint) {
        key.append(res.scheme())
                .append("_").append(res.path())
                .append("_").append(constraint.width())
                .append("_").append(constraint.height());
    }

    @Override
    LruCache<CharBuffer, Snapshot<Bitmap>> delegate() {
        return delegate;
    }

    void clear() {
        delegate().evictAll();
    }

}
