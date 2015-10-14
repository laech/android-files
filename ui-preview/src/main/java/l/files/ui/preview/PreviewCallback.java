package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import l.files.fs.File;

public interface PreviewCallback {

    void onSizeAvailable(File file, Rect size);

    void onPaletteAvailable(File file, Palette palette);

    void onPreviewAvailable(File file, Bitmap thumbnail);

    void onPreviewFailed(File file);

}
