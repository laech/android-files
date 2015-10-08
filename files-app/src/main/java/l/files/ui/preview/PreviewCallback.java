package l.files.ui.preview;

import android.support.v7.graphics.Palette;

import l.files.common.graphics.Rect;
import l.files.fs.File;

public interface PreviewCallback {

    void onSizeAvailable(File item, Rect size);

    void onPaletteAvailable(File item, Palette palette);

    void onPreviewAvailable(File item, Thumbnail thumbnail);

    void onPreviewFailed(File item);

}
