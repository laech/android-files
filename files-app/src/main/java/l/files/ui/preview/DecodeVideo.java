package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;

import com.google.common.net.MediaType;

import l.files.fs.Resource;

final class DecodeVideo extends DecodeMedia
{
    DecodeVideo(
            final Preview context,
            final ImageView view,
            final Resource res,
            final String key)
    {
        super(context, view, res, key);
    }

    static void run(
            final Preview context,
            final ImageView view,
            final Resource res,
            final String key)
    {
        new DecodeVideo(context, view, res, key)
                .executeOnExecutor(SERIAL_EXECUTOR);
    }

    static boolean isVideo(final Resource res, final MediaType media)
    {
        return res.file().isPresent()
                && media.type().equalsIgnoreCase("video");
    }

    @Override
    protected Bitmap decode(final MediaMetadataRetriever retriever)
    {
        return retriever.getFrameAtTime();
    }

}
