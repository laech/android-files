package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.view.View;

import com.google.common.net.MediaType;

import l.files.fs.Resource;
import l.files.fs.Stat;

final class DecodeVideo extends DecodeMedia
{
    DecodeVideo(
            final Preview context,
            final Resource res,
            final Stat stat,
            final View view,
            final PreviewCallback callback,
            final String key)
    {
        super(context, res, stat, view, callback, key);
    }

    static void run(
            final Preview context,
            final Resource res,
            final Stat stat,
            final View view,
            final PreviewCallback callback,
            final String key)
    {
        new DecodeVideo(context, res, stat, view, callback, key)
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
