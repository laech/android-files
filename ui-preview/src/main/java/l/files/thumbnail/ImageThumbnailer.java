package l.files.thumbnail;

import java.io.IOException;
import java.io.InputStream;

import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.ui.base.graphics.Bitmaps.decodeScaledDownBitmap;

public final class ImageThumbnailer implements Thumbnailer<InputStream> {

    @Override
    public ScaledBitmap create(InputStream input, Rect max) throws IOException {
        return decodeScaledDownBitmap(input, max);
    }

}
