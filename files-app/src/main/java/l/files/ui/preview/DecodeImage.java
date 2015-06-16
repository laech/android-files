package l.files.ui.preview;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.common.net.MediaType;

import java.io.InputStream;

import l.files.fs.Resource;
import l.files.common.graphics.Bitmaps;
import l.files.common.graphics.ScaledSize;

import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;

final class DecodeImage extends BitmapTask
{
    final ScaledSize size;

    private DecodeImage(
            final Preview context,
            final ImageView view,
            final Resource res,
            final String key,
            final ScaledSize size)
    {
        super(context, view, res, key);
        this.size = requireNonNull(size, "size");
    }

    static boolean isImage(final MediaType media)
    {
        return media.type().equalsIgnoreCase("image");
    }

    static void run(
            final Preview context,
            final ImageView view,
            final Resource res,
            final String key,
            final ScaledSize size)
    {
        new DecodeImage(context, view, res, key, size)
                .executeOnExecutor(SERIAL_EXECUTOR);
    }

    @Override
    protected Bitmap decode() throws Exception
    {
        try (InputStream in = res.input(FOLLOW))
        {
            return Bitmaps.decode(in, size);
        }
    }
}
