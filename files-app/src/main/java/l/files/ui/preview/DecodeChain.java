package l.files.ui.preview;

import android.graphics.BitmapFactory.Options;
import android.widget.ImageView;

import com.google.common.base.Stopwatch;
import com.google.common.net.MediaType;

import java.io.IOException;
import java.io.InputStream;

import l.files.common.graphics.ScaledSize;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.BitmapFactory.decodeStream;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Objects.requireNonNull;
import static l.files.R.id.image_decorator_task;
import static l.files.common.graphics.Bitmaps.scale;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.ui.preview.DecodeAudio.isAudio;
import static l.files.ui.preview.DecodeImage.isImage;
import static l.files.ui.preview.DecodePdf.isPdf;
import static l.files.ui.preview.DecodeVideo.isVideo;
import static l.files.ui.preview.Preview.errors;
import static l.files.ui.preview.Preview.newPlaceholder;
import static l.files.ui.preview.Preview.sizes;

final class DecodeChain extends Decode<MediaType>
{
    private final Stat stat;

    private DecodeChain(
            final Preview context,
            final ImageView view,
            final Resource res,
            final Stat stat,
            final String key)
    {
        super(context, view, res, key);
        this.stat = requireNonNull(stat, "stat");
    }

    static void run(
            final Preview context,
            final ImageView view,
            final Resource res,
            final Stat stat)
    {
        final Decode task = (Decode) view.getTag(image_decorator_task);
        if (task != null && task.res.equals(res)) return;
        if (task != null) task.cancel(true);

        view.setImageDrawable(null);
        view.setVisibility(GONE);
        view.setTag(image_decorator_task, null);

        if (stat.size() == 0
                || !stat.isRegularFile()
                || !isReadable(res))
        {
            return;
        }

        final String key = context.key(res, stat);
        if (errors.get(key) != null
                || context.setCached(key, view))
        {
            return;
        }

        final ScaledSize size = sizes.get(key);
        final MediaType media = MediaTypes.cached(res, stat);

        if (size == null)
        {
            if (media == null || isImage(media))
            {
                new DecodeChain(context, view, res, stat, key)
                        .executeOnExecutor(THREAD_POOL_EXECUTOR);
            }
            else
            {
                decodeNonImage(context, view, res, key, media);
            }
        }
        else
        {
            view.setVisibility(VISIBLE);
            view.setImageDrawable(newPlaceholder(size));

            if (media != null)
            {
                if (isImage(media))
                {
                    DecodeImage.run(context, view, res, key, size);
                }
                else
                {
                    decodeNonImage(context, view, res, key, media);
                }
            }
            // else media is null - currently there is an instance of this task
            // running, do nothing
        }
    }

    private static void decodeNonImage(
            final Preview context,
            final ImageView view,
            final Resource res,
            final String key,
            final MediaType media)
    {
        if (isPdf(res, media))
        {
            DecodePdf.run(context, view, res, key);
        }
        else if (isAudio(res, media))
        {
            DecodeAudio.run(context, view, res, key);
        }
        else if (isVideo(res, media))
        {
            DecodeVideo.run(context, view, res, key);
        }
    }

    private static boolean isReadable(final Resource resource)
    {
        try
        {
            return resource.readable();
        }
        catch (final IOException e)
        {
            return false;
        }
    }

    @Override
    protected MediaType doInBackground(final Void... params)
    {
        if (isCancelled())
        {
            return null;
        }

        /*
         * Currently decoding the size is quicker than decoding the media type,
         * if decode size failed, then the file is not an image, proceed to
         * decode the media type.
         */

        final ScaledSize size = decodeSize();
        if (size != null)
        {
            publishProgress(size);
        }
        return MediaTypes.detect(res, stat);
    }

    @Override
    protected void onProgressUpdate(final ScaledSize... values)
    {
        super.onProgressUpdate(values);
        DecodeImage.run(context, view, res, key, values[0]);
    }

    private ScaledSize decodeSize()
    {
        final Stopwatch watch = Stopwatch.createStarted();
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        try (InputStream in = res.input(FOLLOW))
        {
            decodeStream(in, null, options);
        }
        catch (final IOException e)
        {
            log.warn(e);
            return null;
        }

        log.debug("decode size took %s for %s", watch, res);
        if (options.outWidth > 0 && options.outHeight > 0)
        {
            return scale(
                    options.outWidth,
                    options.outHeight,
                    context.maxWidth,
                    context.maxHeight);
        }
        return null;
    }

    @Override
    void onSuccess(final MediaType result)
    {
        super.onSuccess(result);
        if (view.getTag(image_decorator_task) == this)
        {
            view.setTag(image_decorator_task, null);
            decodeNonImage(context, view, res, key, result);
        }
    }
}
