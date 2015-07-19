package l.files.ui.preview;

import android.graphics.Bitmap;

import com.google.common.base.Stopwatch;

import java.io.IOException;

import javax.annotation.Nullable;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.Bitmap.createScaledBitmap;
import static l.files.common.base.Stopwatches.startWatchIfDebug;

abstract class DecodeBitmap extends Decode {

  DecodeBitmap(
      Resource res,
      Stat stat,
      Rect constraint,
      PreviewCallback callback,
      Preview context) {
    super(res, stat, constraint, callback, context);
  }

  @Override protected Void doInBackground(Object... params) {
    if (isCancelled()) {
      return null;
    }

    Stopwatch watch = startWatchIfDebug();
    Result result;
    try {
      result = decode();
    } catch (Exception e) {
      log.warn(e);
      return null;
    }

    if (isCancelled()) {
      if (result != null) {
        result.maybeScaled.recycle();
      }
      return null;
    }

    if (result == null) {
      publishProgress(NoPreview.INSTANCE);
      return null;
    }

    Rect scaledSize = result.originalSize.scale(constraint);
    Bitmap scaledBitmap = createScaledBitmap(
        result.maybeScaled,
        scaledSize.width(),
        scaledSize.height(),
        true);

    publishProgress(result.originalSize, scaledBitmap);

    if (result.maybeScaled != scaledBitmap) {
      result.maybeScaled.recycle();
    }

    log.debug("%s, %s", watch, res);

    if (isCancelled()) {
      return null;
    }

    boolean scaledDown =
        result.originalSize.width() > scaledBitmap.getWidth() ||
            result.originalSize.height() > scaledBitmap.getHeight();

    if (scaledDown) {
      try {
        context.putBitmapToDisk(res, stat, constraint, scaledBitmap);
      } catch (IOException e) {
        log.warn(e);
      }
    }

    return null;
  }

  @Nullable abstract Result decode() throws IOException;

  static final class Result {
    final Bitmap maybeScaled;
    final Rect originalSize;

    Result(Bitmap maybeScaled, Rect originalSize) {
      this.maybeScaled = maybeScaled;
      this.originalSize = originalSize;
    }
  }

}