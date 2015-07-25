package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;

public interface PreviewCallback {

  void onSizeAvailable(Resource item, Rect size);

  void onPaletteAvailable(Resource item, Palette palette);

  void onPreviewAvailable(Resource item, Bitmap bitmap);

  void onPreviewFailed(Resource item);

}
