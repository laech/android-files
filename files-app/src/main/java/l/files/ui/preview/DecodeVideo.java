package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.google.common.net.MediaType;

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

  static boolean isVideo(MediaType media, Resource res) {
    return res.file().isPresent()
        && media.type().equalsIgnoreCase("video");
  }

  @Override Bitmap decode(MediaMetadataRetriever retriever) {
    return retriever.getFrameAtTime();
  }

}
