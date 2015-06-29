package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;

import javax.annotation.Nullable;

import l.files.common.graphics.ScaledSize;
import l.files.fs.Resource;

import static android.graphics.Bitmap.createScaledBitmap;
import static l.files.common.graphics.Bitmaps.scale;

abstract class DecodeMedia extends DecodeBitmap
{
    DecodeMedia(
            final Preview context,
            final ImageView view,
            final Resource res,
            final String key)
    {
        super(context, view, res, key);
    }

    @Override
    protected Bitmap decode() throws Exception
    {
        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try
        {
            retriever.setDataSource(res.path());

            final Bitmap original = decode(retriever);
            if (original == null)
            {
                return null;
            }

            final ScaledSize size = scale(
                    original,
                    context.maxWidth,
                    context.maxHeight);

            publishProgress(size);

            final Bitmap scaled = createScaledBitmap(
                    original,
                    size.scaledWidth(),
                    size.scaledHeight(),
                    true);

            if (original != scaled)
            {
                original.recycle();
            }

            return scaled;
        }
        finally
        {
            retriever.release();
        }
    }

    @Nullable
    protected abstract Bitmap decode(MediaMetadataRetriever retriever);

}
