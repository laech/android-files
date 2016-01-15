package l.files.ui.preview;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

import l.files.fs.Path;
import l.files.fs.Stat;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;

final class ThumbnailMemCache extends MemCache<Bitmap> {

    private final LruCache<ByteBuffer, Snapshot<Bitmap>> delegate;

    ThumbnailMemCache(Context context, float appMemoryPercentageToUseForCache) {
        this(calculateSize(context, appMemoryPercentageToUseForCache));
    }

    ThumbnailMemCache(int size) {
        delegate = new LruCache<ByteBuffer, Snapshot<Bitmap>>(size) {
            @Override
            protected int sizeOf(ByteBuffer key, Snapshot<Bitmap> value) {
                if (SDK_INT >= KITKAT) {
                    return value.get().getAllocationByteCount();
                } else {
                    return value.get().getByteCount();
                }
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
    void key(ByteBuffer key, Path path, Stat stat, Rect constraint) {
        path.toByteArray(key.asOutputStream());
        key.putInt(constraint.width())
                .putInt(constraint.height());
    }

    @Override
    LruCache<ByteBuffer, Snapshot<Bitmap>> delegate() {
        return delegate;
    }

    void clear() {
        delegate().evictAll();
    }

}
