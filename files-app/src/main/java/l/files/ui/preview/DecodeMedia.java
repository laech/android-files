package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.google.common.net.MediaType;

import java.io.IOException;

import javax.annotation.Nullable;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

abstract class DecodeMedia extends DecodeBitmap {

  DecodeMedia(
      Resource res,
      Stat stat,
      Rect constraint,
      PreviewCallback callback,
      Preview context) {
    super(res, stat, constraint, callback, context);
  }

  @Override Result decode() throws IOException {
    if (isCancelled()) {
      return null;
    }

    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    try {
      retriever.setDataSource(res.file().get().getPath());

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
