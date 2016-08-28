package l.files.thumbnail;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.ui.base.media.MediaMetadataRetrievers.getAnyThumbnail;

public final class MediaThumbnailer implements Thumbnailer<Path> {

    @Override
    public boolean accepts(Path path, String mediaType) {
        return mediaType.startsWith("audio/") ||
                mediaType.startsWith("video/");
    }

    @Override
    public ScaledBitmap create(Path path, Rect max, Context context) throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            Uri uri = Uri.parse(path.toUri().toString());
            retriever.setDataSource(context, uri);
            return getAnyThumbnail(retriever, max);
        } finally {
            retriever.release();
        }
    }

}
