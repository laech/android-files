package l.files.thumbnail;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.fs.Files.newBufferedInputStream;
import static l.files.ui.base.graphics.Bitmaps.decodeScaledDownBitmap;

public final class ImageThumbnailer implements Thumbnailer {

    @Override
    public ScaledBitmap create(Path path, Rect max) throws IOException {

        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(newBufferedInputStream(path));
            return decodeScaledDownBitmap(in, max);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

}
