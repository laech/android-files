package l.files.thumbnail;

import java.io.InputStream;

import l.files.base.io.Closer;
import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.Files.newInputStream;

public final class PathStreamThumbnailer implements Thumbnailer<Path> {

    private final Thumbnailer<InputStream> thumbnailer;

    public PathStreamThumbnailer(Thumbnailer<InputStream> thumbnailer) {
        this.thumbnailer = requireNonNull(thumbnailer);
    }

    @Override
    public ScaledBitmap create(Path path, Rect max) throws Exception {
        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(newInputStream(path));
            return thumbnailer.create(in, max);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }
}
