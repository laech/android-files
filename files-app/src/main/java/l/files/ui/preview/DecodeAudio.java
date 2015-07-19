package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.google.common.net.MediaType;

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
      Preview context,
      MediaType media) {
    super(res, stat, constraint, callback, context);
  }

  static boolean isAudio(MediaType media, Resource res) {
    return res.file().isPresent()
        && media.type().equalsIgnoreCase("audio");
  }

  @Override Bitmap decode(MediaMetadataRetriever retriever) {
    byte[] data = retriever.getEmbeddedPicture();
    if (data == null) {
      return null;
    }
    return decodeByteArray(data, 0, data.length);
  }

}
