package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

import java.io.IOException;
import java.io.InputStream;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.BitmapFactory.decodeStream;

final class DecodeImage extends DecodeBitmap {

  DecodeImage(
      Resource res,
      Stat stat,
      Rect constraint,
      PreviewCallback callback,
      Preview context) {
    super(res, stat, constraint, callback, context);
  }

  static boolean isImage(String media) {
    return media.startsWith("image/");
  }

  @Override DecodeImage executeOnPreferredExecutor() {
    return (DecodeImage) executeOnExecutor(THREAD_POOL_EXECUTOR);
  }

  @Override Result decode() throws IOException {
    Rect size = context.getSize(res, stat, constraint);
    if (size == null) {
      size = context.decodeSize(res);
      if (size != null) {
        publishProgress(size);
      }
    }

    if (isCancelled()) {
      return null;
    }

    if (size == null) {
      return null;
    }

    try (InputStream in = res.input()) {
      Bitmap bitmap = decodeStream(in, null, options(size));
      return bitmap != null ? new Result(bitmap, size) : null;
    }
  }

  private Options options(Rect original) {
    Rect scaled = original.scale(constraint);
    float scale = scaled.width() / (float) original.width();
    Options options = new Options();
    options.inSampleSize = (int) (1 / scale);
    return options;
  }

}
