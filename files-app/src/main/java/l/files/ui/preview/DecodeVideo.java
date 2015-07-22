package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

final class DecodeVideo extends DecodeMedia {

  DecodeVideo(
      Resource res,
      Stat stat,
      Rect constraint,
      PreviewCallback callback,
      Preview context) {
    super(res, stat, constraint, callback, context);
  }

  static boolean isVideo(String media, Resource res) {
    return res.file().isPresent() && media.startsWith("video/");
  }

  @Override Bitmap decode(MediaMetadataRetriever retriever) {
    return retriever.getFrameAtTime();
  }

}
