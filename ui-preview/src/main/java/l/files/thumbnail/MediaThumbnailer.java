package l.files.thumbnail;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.base.media.MediaMetadataRetrievers.getAnyThumbnail;

public final class MediaThumbnailer implements Thumbnailer<Path> {

    private final Context context;

    public MediaThumbnailer(Context context) {
        this.context = requireNonNull(context, "context");
    }

    @Override
    public ScaledBitmap create(Path path, Rect max) throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            setDataSource(retriever, path);
            return getAnyThumbnail(retriever, max);
        } finally {
            retriever.release();
        }
    }

    private void setDataSource(MediaMetadataRetriever retriever, Path path) {
        Uri uri = Uri.parse(path.toUri().toString());
        retriever.setDataSource(context, uri);
    }

}
