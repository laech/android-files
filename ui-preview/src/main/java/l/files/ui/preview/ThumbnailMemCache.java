package l.files.ui.preview;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import java.util.AbstractMap.SimpleImmutableEntry;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;

final class ThumbnailMemCache extends MemCache<Object, Bitmap> {

    private final LruCache<Object, Snapshot<Bitmap>> delegate;
    private final boolean keyIncludeConstraint;

    ThumbnailMemCache(
            Context context,
            boolean keyIncludeConstraint,
            float appMemoryPercentageToUseForCache) {

        this(
                calculateSize(context, appMemoryPercentageToUseForCache),
                keyIncludeConstraint
        );
    }

    ThumbnailMemCache(int size, boolean keyIncludeConstraint) {
        this.keyIncludeConstraint = keyIncludeConstraint;
        this.delegate = new LruCache<Object, Snapshot<Bitmap>>(size) {
            @Override
            protected int sizeOf(Object key, Snapshot<Bitmap> value) {
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
    Object getKey(Path path, Rect constraint) {
        return keyIncludeConstraint
                ? new SimpleImmutableEntry<>(path, constraint)
                : path;
    }

    @Override
    LruCache<Object, Snapshot<Bitmap>> delegate() {
        return delegate;
    }

    void clear() {
        delegate().evictAll();
    }

}
