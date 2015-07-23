package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.Executor;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static java.util.concurrent.Executors.newFixedThreadPool;

abstract class DecodeMedia extends DecodeBitmap {

  // No need to set UncaughtExceptionHandler to terminate
  // on exception already set by Android
  private static final Executor executor = newFixedThreadPool(2);

  DecodeMedia(
      Resource res,
      Stat stat,
      Rect constraint,
      PreviewCallback callback,
      Preview context) {
    super(res, stat, constraint, callback, context);
  }

  @Override DecodeMedia executeOnPreferredExecutor() {
    return (DecodeMedia) executeOnExecutor(executor);
  }

  @Override Result decode() throws IOException {
    if (isCancelled()) {
      return null;
    }

    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    try {
      retriever.setDataSource(res.file().getPath());

      if (isCancelled()) {
        return null;
      }

      Bitmap bitmap = decode(retriever);
      if (bitmap != null) {
        Rect size = Rect.of(bitmap.getWidth(), bitmap.getHeight());
        return new Result(bitmap, size);
      }

    } finally {
      retriever.release();
    }
    return null;
  }

  @Nullable abstract Bitmap decode(MediaMetadataRetriever retriever);

}
