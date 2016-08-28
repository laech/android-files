package l.files.thumbnail;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.fs.Files.newInputStream;
import static l.files.ui.base.graphics.Bitmaps.decodeScaledDownBitmap;

public final class ImageThumbnailer implements Thumbnailer<Path> {

    @Override
    public ScaledBitmap create(final Path path, Rect max) throws Exception {
        return decodeScaledDownBitmap(new Callable<InputStream>() {
            @Override
            public InputStream call() throws IOException {
                return newInputStream(path);
            }
        }, max);
    }

}
