package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.view.View;

import com.google.common.net.MediaType;

import java.io.InputStream;

import l.files.common.graphics.ScaledSize;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.BitmapFactory.decodeStream;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;

final class DecodeImage extends DecodeBitmap
{
    final ScaledSize size;

    DecodeImage(
            final Preview context,
            final Resource res,
            final Stat stat,
            final View view,
            final PreviewCallback callback,
            final String key,
            final ScaledSize size)
    {
        super(context, res, stat, view, callback, key);
        this.size = requireNonNull(size, "size");
    }

    static boolean isImage(final MediaType media)
    {
        return media.type().equalsIgnoreCase("image");
    }

    static void run(
            final Preview context,
            final Resource res,
            final Stat stat,
            final View view,
            final PreviewCallback callback,
            final String key,
            final ScaledSize size)
    {
        new DecodeImage(context, res, stat, view, callback, key, size)
                .executeOnExecutor(SERIAL_EXECUTOR);
    }

    @Override
    protected Bitmap decode() throws Exception
    {
        try (InputStream in = res.input(FOLLOW))
        {
            return decodeStream(in, null, options(size));
        }
    }

    private static Options options(final ScaledSize size)
    {
        final Options options = new Options();
        options.inSampleSize = (int) (1 / size.scale());
        return options;
    }
}
