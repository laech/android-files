package l.files.thumbnail;

import android.content.Context;
import android.media.MediaMetadataRetriever;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.ui.base.media.MediaMetadataRetrievers.getAnyThumbnail;

final class MediaThumbnailer implements Thumbnailer<Path> {

    @Override
    public boolean accepts(Path path, String mediaType) {
        return mediaType.startsWith("audio/") ||
                mediaType.startsWith("video/");
    }

    @Override
    public ScaledBitmap create(Path path, Rect max, Context context)
            throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, path.toUri());
            return getAnyThumbnail(retriever, max);
        } finally {
            retriever.release();
        }
    }

}
