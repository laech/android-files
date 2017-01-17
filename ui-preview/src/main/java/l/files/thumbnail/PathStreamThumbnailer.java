package l.files.thumbnail;

import android.content.Context;

import java.io.InputStream;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.base.Objects.requireNonNull;

public final class PathStreamThumbnailer implements Thumbnailer<Path> {

    private final Thumbnailer<InputStream> thumbnailer;

    public PathStreamThumbnailer(Thumbnailer<InputStream> thumbnailer) {
        this.thumbnailer = requireNonNull(thumbnailer);
    }

    @Override
    public boolean accepts(Path path, String mediaType) {
        return thumbnailer.accepts(path, mediaType);
    }

    @Override
    public ScaledBitmap create(Path path, Rect max, Context context) throws Exception {
        InputStream in = path.newInputStream();
        try {
            return thumbnailer.create(in, max, context);
        } finally {
            in.close();
        }
    }
}
