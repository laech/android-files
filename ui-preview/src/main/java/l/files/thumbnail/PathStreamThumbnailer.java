package l.files.thumbnail;

import java.io.InputStream;

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
        InputStream in = newInputStream(path);
        try {
            return thumbnailer.create(in, max);
        } finally {
            in.close();
        }
    }
}
