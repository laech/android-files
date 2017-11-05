package l.files.thumbnail;

import android.content.Context;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.ui.base.graphics.Bitmaps.decodeScaledDownBitmap;

final class ImageThumbnailer implements Thumbnailer<Path> {

    @Override
    public boolean accepts(Path path, String mediaType) {
        return mediaType.startsWith("image/");
    }

    @Override
    public ScaledBitmap create(Path path, Rect max, Context context)
            throws Exception {
        return decodeScaledDownBitmap(path::newInputStream, max);
    }

}
