package l.files.ui.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.io.InputStream;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.fs.local.LocalResource;
import l.files.logging.Logger;

import static android.graphics.BitmapFactory.decodeStream;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;

public final class Preview {

  private static final Logger log = Logger.get(Preview.class);
  private static Preview instance;

  public static Preview get(Context context) {
    synchronized (Preview.class) {
      if (instance == null) {
        instance = new Preview(context);
      }
    }
    return instance;
  }

  private final PersistenceCache<Rect> sizeCache;
  private final PersistenceCache<String> mediaTypeCache;
  private final PersistenceCache<Boolean> noPreviewCache;
  private final ThumbnailMemCache thumbnailMemCache;
  private final ThumbnailDiskCache thumbnailDiskCache;

  final DisplayMetrics displayMetrics;
  final Resource cacheDir;

  private Preview(Context context) {
    this.cacheDir = LocalResource.create(context.getExternalCacheDir());
    this.displayMetrics = requireNonNull(context).getResources().getDisplayMetrics();
    this.sizeCache = new RectCache(cacheDir);
    this.mediaTypeCache = new MediaTypeCache(cacheDir);
    this.noPreviewCache = new NoPreviewCache(cacheDir);
    this.thumbnailMemCache = new ThumbnailMemCache(context, 0.3f);
    this.thumbnailDiskCache = new ThumbnailDiskCache(cacheDir);
  }

  public void writeCacheAsyncIfNeeded() {
    sizeCache.writeAsyncIfNeeded();
    mediaTypeCache.writeAsyncIfNeeded();
    noPreviewCache.writeAsyncIfNeeded();
  }

  public void readCacheAsyncIfNeeded() {
    sizeCache.readAsyncIfNeeded();
    mediaTypeCache.readAsyncIfNeeded();
    noPreviewCache.readAsyncIfNeeded();
  }

  public void cleanupAsync() {
    thumbnailDiskCache.cleanupAsync();
  }

  @Nullable public Bitmap getBitmap(Resource res, Stat stat, Rect constraint) {
    return thumbnailMemCache.get(res, stat, constraint);
  }

  void putBitmap(Resource res, Stat stat, Rect constraint, Bitmap bitmap) {
    thumbnailMemCache.put(res, stat, constraint, bitmap);
  }

  @Nullable
  Bitmap getBitmapFromDisk(Resource res, Stat stat, Rect constraint) throws IOException {
    return thumbnailDiskCache.get(res, stat, constraint);
  }

  void putBitmapToDiskAsync(
      Resource res, Stat stat, Rect constraint, Bitmap bitmap) {
    thumbnailDiskCache.putAsync(res, stat, constraint, bitmap);
  }

  @Nullable public Rect getSize(Resource res, Stat stat, Rect constraint) {
    return sizeCache.get(res, stat, constraint);
  }

  void putSize(Resource res, Stat stat, Rect constraint, Rect size) {
    sizeCache.put(res, stat, constraint, size);
  }

  @Nullable String getMediaType(Resource res, Stat stat, Rect constraint) {
    return mediaTypeCache.get(res, stat, constraint);
  }

  void putMediaType(Resource res, Stat stat, Rect constraint, String media) {
    mediaTypeCache.put(res, stat, constraint, media);
  }

  public boolean isPreviewable(Resource res, Stat stat, Rect constraint) {
    return stat.size() > 0
        && stat.isRegularFile()
        && isReadable(res)
        && !TRUE.equals(noPreviewCache.get(res, stat, constraint));
  }

  void putPreviewable(Resource res, Stat stat, Rect constraint, boolean previewable) {
    if (previewable) {
      noPreviewCache.remove(res, stat, constraint);
    } else {
      noPreviewCache.put(res, stat, constraint, true);
    }
  }

  private static boolean isReadable(Resource resource) {
    try {
      return resource.readable();
    } catch (IOException e) {
      return false;
    }
  }

  @Nullable public Decode set(
      Resource res,
      Stat stat,
      Rect constraint,
      PreviewCallback callback) {
    return DecodeChain.run(res, stat, constraint, callback, this);
  }

  Rect decodeSize(Resource res) {
    log.debug("decode size start %s", res);
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;

    try (InputStream in = res.input(FOLLOW)) {
      decodeStream(in, null, options);

    } catch (Exception e) {
      log.warn(e);
      return null;
    }

    log.debug("decode size end %s", res);

    if (options.outWidth > 0 && options.outHeight > 0) {
      return Rect.of(options.outWidth, options.outHeight);
    }
    return null;
  }

}
