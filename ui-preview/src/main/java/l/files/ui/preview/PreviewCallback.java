package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import l.files.fs.File;
import l.files.fs.Stat;

public interface PreviewCallback {

    void onSizeAvailable(File file, Stat stat, Rect size);

    void onPaletteAvailable(File file, Stat stat, Palette palette);

    void onPreviewAvailable(File file, Stat stat, Bitmap thumbnail);

    void onPreviewFailed(File file, Stat stat);

}
