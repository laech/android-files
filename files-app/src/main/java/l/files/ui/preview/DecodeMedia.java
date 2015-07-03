package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.view.View;

import javax.annotation.Nullable;

import l.files.fs.Resource;
import l.files.fs.Stat;

abstract class DecodeMedia extends DecodeBitmap
{
    DecodeMedia(
            final Preview context,
            final Resource res,
            final Stat stat,
            final View view,
            final PreviewCallback callback,
            final String key)
    {
        super(context, res, stat, view, callback, key);
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
