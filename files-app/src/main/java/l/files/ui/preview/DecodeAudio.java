package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;

import com.google.common.net.MediaType;

import l.files.fs.Resource;

import static android.graphics.BitmapFactory.decodeByteArray;

final class DecodeAudio extends DecodeMedia
{
    DecodeAudio(
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
        new DecodeAudio(context, view, res, key)
                .executeOnExecutor(SERIAL_EXECUTOR);
    }

    static boolean isAudio(final Resource res, final MediaType media)
    {
        return res.file().isPresent()
                && media.type().equalsIgnoreCase("audio");
    }

    @Override
    protected Bitmap decode(final MediaMetadataRetriever retriever)
    {
        final byte[] data = retriever.getEmbeddedPicture();
        if (data == null)
        {
            return null;
        }
        return decodeByteArray(data, 0, data.length);
    }

}
