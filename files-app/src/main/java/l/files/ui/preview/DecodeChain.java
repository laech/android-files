package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.view.View;

import com.google.common.base.Stopwatch;
import com.google.common.net.MediaType;

import java.io.IOException;
import java.io.InputStream;

import l.files.common.graphics.ScaledSize;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.BitmapFactory.decodeStream;
import static l.files.R.id.image_decorator_task;
import static l.files.common.graphics.Bitmaps.scale;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.ui.preview.DecodeAudio.isAudio;
import static l.files.ui.preview.DecodeImage.isImage;
import static l.files.ui.preview.DecodePdf.isPdf;
import static l.files.ui.preview.DecodeVideo.isVideo;
import static l.files.ui.preview.Preview.errors;
import static l.files.ui.preview.Preview.sizes;

final class DecodeChain extends Decode<MediaType>
{
    DecodeChain(
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
            final PreviewCallback callback)
    {
        final Decode task = (Decode) view.getTag(image_decorator_task);
        if (task != null && task.res.equals(res)) return;
        if (task != null) task.cancel(true);

        view.setTag(image_decorator_task, null);

        if (stat.size() == 0
                || !stat.isRegularFile()
                || !isReadable(res))
        {
            return;
        }

        final String key = context.key(res, stat);
        if (errors.get(key) != null)
        {
            return;
        }

        Bitmap cached = context.memCache.get(key);
        if (cached != null)
        {
            callback.onPreviewAvailable(res, cached);
            return;
        }

        final ScaledSize size = sizes.get(key);
        final MediaType media = MediaTypes.cached(res, stat);

        if (size == null)
        {
            if (media == null || isImage(media))
            {
                new DecodeChain(context, res, stat, view, callback, key)
                        .executeOnExecutor(THREAD_POOL_EXECUTOR);
            }
            else
            {
                decodeNonImage(context, res, stat, view, callback, key, media);
            }
        }
        else
        {
            callback.onSizeAvailable(res, size);

            if (media != null)
            {
                if (isImage(media))
                {
                    DecodeImage.run(context, res, stat, view, callback, key, size);
                }
                else
                {
                    decodeNonImage(context, res, stat, view, callback, key, media);
                }
            }
            // else media is null - currently there is an instance of this task
            // running, do nothing
        }
    }

    private static void decodeNonImage(
            final Preview context,
            final Resource res,
            final Stat stat,
            final View view,
            final PreviewCallback callback,
            final String key,
            final MediaType media)
    {
        if (isPdf(res, media))
        {
            DecodePdf.run(context, res, stat, view, callback, key);
        }
        else if (isAudio(res, media))
        {
            DecodeAudio.run(context, res, stat, view, callback, key);
        }
        else if (isVideo(res, media))
        {
            DecodeVideo.run(context, res, stat, view, callback, key);
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
        DecodeImage.run(context, res, stat, view, callback, key, values[0]);
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
            decodeNonImage(context, res, stat, view, callback, key, result);
        }
    }
}
