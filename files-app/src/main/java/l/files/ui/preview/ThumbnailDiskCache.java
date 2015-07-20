package l.files.ui.preview;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.logging.Logger;

import static android.graphics.Bitmap.CompressFormat.WEBP;
import static android.graphics.BitmapFactory.decodeStream;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.common.base.Stopwatches.startWatchIfDebug;

final class ThumbnailDiskCache extends Cache<Bitmap> {

  private static final Logger log = Logger.get(ThumbnailDiskCache.class);

  // No need to set UncaughtExceptionHandler to terminate
  // on exception already set by Android
  private static final Executor executor =
      newFixedThreadPool(2, new ThreadFactoryBuilder()
          .setNameFormat("thumbnail-disk-cache-pool-thread-%d")
          .build());

  /**
   * Place a dummy byte at the beginning of the cache files,
   * make them unrecognizable as image files, as to make them
   * not previewable, so won't get into the situation of previewing
   * the cache, save the thumbnail of the cache, preview the cache
   * of the cache...
   */
  private static final int DUMMY_BYTE = 0;

  private final File cacheDir;

  // TODO don't save anything from cache dir

  ThumbnailDiskCache(Context context) {
    this.cacheDir = new File(context.getExternalCacheDir(), "thumbnails");
  }

  private File cache(Resource res, Stat stat, Rect constraint) {
    return new File(cacheDir, res.scheme()
        + "/" + res.path()
        + "_" + stat.mtime().seconds()
        + "_" + stat.mtime().nanos()
        + "_" + constraint.width()
        + "_" + constraint.height());
  }

  @Override Bitmap get(
      Resource res,
      Stat stat,
      Rect constraint) throws IOException {

    Stopwatch watch = startWatchIfDebug();
    File cache = cache(res, stat, constraint);
    try (InputStream in = new BufferedInputStream(new FileInputStream(cache))) {
      in.read(); // read DUMMY_BYTE

      Bitmap bitmap = decodeStream(in);
      log.debug("read bitmap %s %s", watch, res);

      if (bitmap == null) {
        return null;
      }

      if (bitmap.getWidth() > constraint.width() ||
          bitmap.getHeight() > constraint.height()) {
        bitmap.recycle();
        return null;
      }

      return bitmap;

    } catch (FileNotFoundException e) {
      return null;
    }
  }

  @Override Snapshot<Bitmap> put(
      Resource res,
      Stat stat,
      Rect constraint,
      Bitmap bitmap) throws IOException {

    Stopwatch watch = startWatchIfDebug();
    File cache = cache(res, stat, constraint);
    cache.getParentFile().mkdirs();
    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(cache))) {
      out.write(DUMMY_BYTE);
      bitmap.compress(WEBP, 90, out);
    }
    log.debug("write %s %s", watch, res);
    return null;
  }

  public void putAsync(Resource res, Stat stat, Rect constraint, Bitmap bitmap) {
    executor.execute(new WriteBitmap(
        res, stat, constraint, new WeakReference<>(bitmap)));
  }

  private final class WriteBitmap implements Runnable {
    private final Resource res;
    private final Stat stat;
    private final Rect constraint;
    private final WeakReference<Bitmap> ref;

    private WriteBitmap(
        Resource res,
        Stat stat,
        Rect constraint,
        WeakReference<Bitmap> ref) {
      this.res = requireNonNull(res);
      this.stat = requireNonNull(stat);
      this.constraint = requireNonNull(constraint);
      this.ref = requireNonNull(ref);
    }

    @Override public void run() {
      Bitmap bitmap = ref.get();
      if (bitmap != null) {
        try {
          put(res, stat, constraint, bitmap);
        } catch (IOException e) {
          log.error(e);
        }
      }
    }
  }

}
