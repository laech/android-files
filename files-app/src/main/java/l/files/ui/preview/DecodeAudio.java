package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.BitmapFactory.decodeByteArray;

final class DecodeAudio extends DecodeMedia {

  DecodeAudio(
      Resource res,
      Stat stat,
      Rect constraint,
      PreviewCallback callback,
      Preview context) {
    super(res, stat, constraint, callback, context);
  }

  static boolean isAudio(String media, Resource res) {
    return res.file() != null && media.startsWith("audio/");
  }

  @Override Bitmap decode(MediaMetadataRetriever retriever) {
    byte[] data = retriever.getEmbeddedPicture();
    if (data == null) {
      return null;
    }
    return decodeByteArray(data, 0, data.length);
  }

}
