package l.files.ui.preview;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.google.common.base.Preconditions.checkArgument;

final class ThumbnailMemCache extends MemCache<Bitmap> {

  private final LruCache<String, Snapshot<Bitmap>> delegate;

  ThumbnailMemCache(Context context, float appMemoryPercentageToUseForCache) {
    checkArgument(appMemoryPercentageToUseForCache > 0);
    checkArgument(appMemoryPercentageToUseForCache < 1);

    ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
    int megabytes = manager.getMemoryClass();
    int bytes = megabytes * 1024 * 1024;
    int size = (int) (bytes * appMemoryPercentageToUseForCache);
    delegate = new LruCache<String, Snapshot<Bitmap>>(size) {
      @Override protected int sizeOf(String key, Snapshot<Bitmap> value) {
        return value.get().getByteCount();
      }
    };
  }

  @Override String key(Resource res, Stat stat, Rect constraint) {
    return res.scheme()
        + "_" + res.path()
        + "_" + stat.mtime().seconds()
        + "_" + stat.mtime().nanos()
        + "_" + constraint.width()
        + "_" + constraint.height();
  }

  @Override LruCache<String, Snapshot<Bitmap>> delegate() {
    return delegate;
  }
}
