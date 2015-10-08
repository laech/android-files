package l.files.ui.preview;

import android.app.ActivityManager;
import android.content.Context;
import android.util.LruCache;

import l.files.common.graphics.Rect;
import l.files.fs.File;
import l.files.fs.Stat;

import static android.content.Context.ACTIVITY_SERVICE;

final class ThumbnailMemCache extends MemCache<Thumbnail> {

    private final LruCache<String, Snapshot<Thumbnail>> delegate;

    ThumbnailMemCache(Context context, float appMemoryPercentageToUseForCache) {
        if (appMemoryPercentageToUseForCache <= 0 ||
                appMemoryPercentageToUseForCache >= 1) {
            throw new IllegalArgumentException();
        }

        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        int megabytes = manager.getMemoryClass();
        int bytes = megabytes * 1024 * 1024;
        int size = (int) (bytes * appMemoryPercentageToUseForCache);
        delegate = new LruCache<String, Snapshot<Thumbnail>>(size) {
            @Override
            protected int sizeOf(String key, Snapshot<Thumbnail> value) {
                return value.get().bitmap.getByteCount();
            }
        };
    }

    @Override
    String key(File res, Stat stat, Rect constraint) {
        return res.scheme()
                + "_" + res.path()
                + "_" + stat.lastModifiedTime().seconds()
                + "_" + stat.lastModifiedTime().nanos()
                + "_" + constraint.width()
                + "_" + constraint.height();
    }

    @Override
    LruCache<String, Snapshot<Thumbnail>> delegate() {
        return delegate;
    }

    void clear() {
        delegate().evictAll();
    }
}
