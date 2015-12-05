package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import l.files.fs.Path;
import l.files.fs.Stat;

public interface PreviewCallback {

    void onSizeAvailable(Path path, Stat stat, Rect size);

    void onPaletteAvailable(Path path, Stat stat, Palette palette);

    void onPreviewAvailable(Path path, Stat stat, Bitmap thumbnail);

    void onPreviewFailed(Path path, Stat stat);

}
