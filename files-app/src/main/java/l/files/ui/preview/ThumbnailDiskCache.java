package l.files.ui.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.google.common.base.Stopwatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import l.files.common.graphics.Rect;
import l.files.fs.NotExist;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.fs.local.LocalResource;
import l.files.logging.Logger;

import static android.graphics.Bitmap.createBitmap;
import static l.files.common.base.Stopwatches.startWatchIfDebug;
import static l.files.fs.LinkOption.NOFOLLOW;

final class ThumbnailDiskCache extends Cache<Bitmap> {

  private static final Logger log = Logger.get(ThumbnailDiskCache.class);

  private final Resource cacheDir;

  ThumbnailDiskCache(Context context) {
    this.cacheDir = LocalResource.create(
        context.getExternalCacheDir()).resolve("thumbnails");
  }

  private Resource cache(Resource res, Stat stat, Rect constraint) {
    return cacheDir.resolve(res.scheme()
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
    Resource cache = cache(res, stat, constraint);
    try (DataInputStream in = input(cache)) {

      Bitmap bitmap = read(in, constraint);
      log.debug("read bitmap %s %s", watch, res);
      return bitmap;

    } catch (FileNotFoundException | NotExist e) {
      return null;
    }
  }

  private Bitmap read(DataInput in, Rect constraint) throws IOException {
    int width = in.readInt();
    int height = in.readInt();
    if (width <= 0
        || height <= 0
        || constraint.width() < width
        || constraint.height() < height) {
      throw new InvalidObjectException(width + "x" + height);
    }

    String configName = in.readUTF();
    Config config;
    try {
      config = Config.valueOf(configName);
    } catch (IllegalArgumentException e) {
      throw new InvalidObjectException(configName);
    }

    int length = in.readInt();
    if (length <= 0 || length > width * height * 4) {
      throw new InvalidObjectException(String.valueOf(length));
    }

    byte[] bytes = new byte[length];
    in.readFully(bytes);

    Bitmap bitmap = createBitmap(width, height, config);
    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));

    return bitmap;
  }

  @Override Snapshot<Bitmap> put(
      Resource res,
      Stat stat,
      Rect constraint,
      Bitmap bitmap) throws IOException {

    Stopwatch watch = startWatchIfDebug();
    Resource cache = cache(res, stat, constraint);
    cache.createFiles();
    try (DataOutputStream out = output(cache)) {
      write(out, bitmap);
    }
    log.debug("write %s %s", watch, res);
    return null;
  }

  private void write(DataOutput out, Bitmap bitmap) throws IOException {
    out.writeInt(bitmap.getWidth());
    out.writeInt(bitmap.getHeight());
    out.writeUTF(bitmap.getConfig().name());
    out.writeInt(bitmap.getByteCount());
    out.write(toBytes(bitmap));
  }

  private byte[] toBytes(Bitmap bitmap) {
    byte[] bytes = new byte[bitmap.getByteCount()];
    bitmap.copyPixelsToBuffer(ByteBuffer.wrap(bytes));
    return bytes;
  }

  private DataInputStream input(Resource res) throws IOException {
    return new DataInputStream(new BufferedInputStream(res.input(NOFOLLOW)));
  }

  private DataOutputStream output(Resource res) throws IOException {
    return new DataOutputStream(new BufferedOutputStream(res.output(NOFOLLOW)));
  }

}
