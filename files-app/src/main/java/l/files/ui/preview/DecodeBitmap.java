package l.files.ui.preview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;

import com.google.common.base.Stopwatch;

import l.files.common.graphics.ScaledSize;
import l.files.fs.Resource;

import static android.R.integer.config_shortAnimTime;
import static android.graphics.Bitmap.createScaledBitmap;
import static android.graphics.Color.TRANSPARENT;
import static android.view.View.VISIBLE;
import static l.files.R.id.image_decorator_task;
import static l.files.common.graphics.Bitmaps.scale;

abstract class DecodeBitmap extends Decode<Bitmap>
{
    DecodeBitmap(
            final Preview context,
            final ImageView view,
            final Resource res,
            final String key)
    {
        super(context, view, res, key);
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
    void onSuccess(final Bitmap bitmap)
    {
        context.cache(key, bitmap);
        if (view.getTag(image_decorator_task) == this)
        {
            view.setTag(image_decorator_task, null);
            final Resources res = view.getResources();
            final TransitionDrawable drawable = new TransitionDrawable(
                    new Drawable[]{
                            new ColorDrawable(TRANSPARENT),
                            new BitmapDrawable(res, bitmap)
                    }
            );
            view.setImageDrawable(drawable);
            final int duration = res.getInteger(config_shortAnimTime);
            drawable.startTransition(duration);
            view.setVisibility(VISIBLE);
        }
    }
}
