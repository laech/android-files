package l.files.ui.preview;

import android.graphics.Bitmap;
import android.view.View;

import com.google.common.base.Stopwatch;

import l.files.common.graphics.ScaledSize;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.Bitmap.createScaledBitmap;
import static l.files.R.id.image_decorator_task;
import static l.files.common.graphics.Bitmaps.scale;

abstract class DecodeBitmap extends Decode<Bitmap>
{
    DecodeBitmap(
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
    protected Bitmap doInBackground(final Void... params)
    {
        if (isCancelled())
        {
            return null;
        }

        try
        {
            final Stopwatch watch = Stopwatch.createStarted();
            final Bitmap original = decode();

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

            log.debug("%s, %s", watch, res);

            return scaled;
        }
        catch (final Exception e)
        {
            // Catch all unexpected internal errors from decoder
            log.warn(e);
            return null;
        }
    }

    protected abstract Bitmap decode() throws Exception;

    @Override
    void onSuccess(final Bitmap result)
    {
        context.memCache.put(key, result);
        if (view.getTag(image_decorator_task) == this)
        {
            view.setTag(image_decorator_task, null);
            callback.onPreviewAvailable(res, result);
        }
    }
}
