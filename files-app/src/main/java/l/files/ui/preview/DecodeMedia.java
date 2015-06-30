package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;

import javax.annotation.Nullable;

import l.files.fs.Resource;

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
            return decode(retriever);
        }
        finally
        {
            retriever.release();
        }
    }

    @Nullable
    protected abstract Bitmap decode(MediaMetadataRetriever retriever);

}
