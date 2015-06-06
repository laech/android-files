package l.files.ui.browser;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import l.files.R;
import l.files.common.graphics.drawable.SizedColorDrawable;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.logging.Logger;
import l.files.ui.util.ScaledSize;

import static android.R.integer.config_shortAnimTime;
import static android.content.ContentResolver.SCHEME_FILE;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.BitmapFactory.Options;
import static android.graphics.BitmapFactory.decodeStream;
import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Color.WHITE;
import static android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY;
import static android.os.AsyncTask.SERIAL_EXECUTOR;
import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static android.os.ParcelFileDescriptor.open;
import static android.util.TypedValue.COMPLEX_UNIT_PT;
import static android.util.TypedValue.applyDimension;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.ui.util.Bitmaps.decodeScaledBitmap;
import static l.files.ui.util.Bitmaps.scaleSize;

final class ImageDecorator
{

    private static final Logger logger = Logger.get(ImageDecorator.class);

    private static final Set<Object> errors = new HashSet<>();
    private static final Map<Object, ScaledSize> sizes = new HashMap<>();

    private final LruCache<Object, Bitmap> cache;
    private final int maxWidth;
    private final int maxHeight;

    ImageDecorator(
            final LruCache<Object, Bitmap> cache,
            final int maxWidth,
            final int maxHeight)
    {
        checkArgument(maxWidth > 0);
        checkArgument(maxHeight > 0);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.cache = requireNonNull(cache, "cache");
    }

    public void decorate(
            final ImageView view,
            final Resource resource,
            final Stat stat)
    {
        final Object key = buildCacheKey(resource);

        final Task task = (Task) view.getTag(R.id.image_decorator_task);
        if (task != null && task.resource().equals(resource))
            return;
        if (task != null) task.cancel(true);

        view.setImageDrawable(null);
        view.setVisibility(GONE);
        view.setTag(R.id.image_decorator_task, null);

        if (!isReadable(resource) || !stat.isRegularFile()) return;
        if (errors.contains(key)) return;
        if (setCachedBitmap(view, key)) return;

        final ScaledSize size = sizes.get(key);
        if (size != null)
        {
            view.setVisibility(VISIBLE);
            view.setImageDrawable(newPlaceholder(size));
        }

        // Disabled.
        // PdfRenderer has a bug will cause native crash in constructor, to
        // test this, enable this, then copy a few files that are not PDFs,
        // rename them to have .pdf extension, rotate screen a few times, then
        // copy some of those PDFs to create more PDFs - then it will crash.
//        if (isPdf(resource))
//        {
//            new DecodePdf(key, view, resource)
//                    .executeOnExecutor(SERIAL_EXECUTOR);
//        }
//        else
//        {
        new DecodeImage(key, view, resource, size)
                .executeOnExecutor(SERIAL_EXECUTOR);
//        }
    }

    private boolean isReadable(final Resource resource)
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

    private boolean setCachedBitmap(final ImageView image, final Object key)
    {
        final Bitmap bitmap = cache.get(key);
        if (bitmap != null)
        {
            image.setImageBitmap(bitmap);
            image.setVisibility(VISIBLE);
            return true;
        }
        return false;
    }

    private Object buildCacheKey(final Resource resource)
    {
        return resource.uri() + "?bounds=" + maxWidth + "x" + maxHeight;
    }

    private SizedColorDrawable newPlaceholder(final ScaledSize size)
    {
        return new SizedColorDrawable(
                TRANSPARENT,
                size.scaledWidth,
                size.scaledHeight);
    }

    private interface Task
    {

        Resource resource();

        boolean cancel(boolean mayInterruptIfRunning);

    }

    private static InputStream input(final Resource res) throws IOException
    {
        return res.input(FOLLOW);
    }

    private abstract class DecodeBase
            extends AsyncTask<Void, ScaledSize, Bitmap> implements Task
    {
        final Object key;
        final ImageView view;
        final Resource resource;

        DecodeBase(
                final Object key,
                final ImageView view,
                final Resource resource)
        {
            this.key = key;
            this.view = view;
            this.resource = resource;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            view.setTag(R.id.image_decorator_task, this);
        }

        @Override
        protected void onProgressUpdate(final ScaledSize... values)
        {
            super.onProgressUpdate(values);
            final ScaledSize size = values[0];
            sizes.put(key, size);
            if (view.getTag(R.id.image_decorator_task) == this)
            {
                view.setImageDrawable(newPlaceholder(size));
                view.setVisibility(VISIBLE);
            }
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
                return decode();
            }
            catch (final Exception e)
            {
                // Catch all unexpected internal errors from decoder
                // Include those that aren't declared
                logger.warn(e, "Failed to decode %s", resource);
                return null;
            }
        }

        protected abstract Bitmap decode() throws Exception;

        @Override
        protected void onPostExecute(final Bitmap bitmap)
        {
            super.onPostExecute(bitmap);
            if (bitmap == null)
            {
                onFailed();
            }
            else
            {
                onSuccess(bitmap);
            }
        }

        private void onFailed()
        {
            errors.add(key);
            sizes.remove(key);
            if (view.getTag(R.id.image_decorator_task) == this)
            {
                view.setVisibility(GONE);
                view.setTag(R.id.image_decorator_task, null);
            }
        }

        private void onSuccess(final Bitmap bitmap)
        {
            if (view.getTag(R.id.image_decorator_task) == this)
            {
                view.setTag(R.id.image_decorator_task, null);
                final Resources res = view.getResources();
                final TransitionDrawable drawable =
                        new TransitionDrawable(new Drawable[]{
                                new ColorDrawable(TRANSPARENT),
                                new BitmapDrawable(res, bitmap)
                        });
                view.setImageDrawable(drawable);
                view.setVisibility(VISIBLE);
                drawable.startTransition(res.getInteger(config_shortAnimTime));
            }
            cache.put(key, bitmap);
        }

        @Override
        public Resource resource()
        {
            return resource;
        }
    }

    private final class DecodeImage extends DecodeBase
    {
        private final ScaledSize cachedSize;

        DecodeImage(
                final Object key,
                final ImageView view,
                final Resource resource,
                @Nullable final ScaledSize cachedSize)
        {
            super(key, view, resource);
            this.cachedSize = cachedSize;
        }

        @Override
        protected Bitmap decode() throws Exception
        {
            final ScaledSize size =
                    cachedSize == null
                            ? decodeSize()
                            : cachedSize;

            if (size == null)
            {
                return null;
            }

            if (cachedSize == null)
            {
                publishProgress(size);
            }

            try (InputStream in = input(resource))
            {
                return decodeScaledBitmap(in, size);
            }
        }

        private ScaledSize decodeSize()
        {
            final Options options = new Options();
            options.inJustDecodeBounds = true;
            try (InputStream in = input(resource))
            {
                decodeStream(in, null, options);
            }
            catch (final IOException e)
            {
//                logger.warn(e);
                return null;
            }

            if (options.outWidth <= 0 || options.outHeight <= 0)
            {
                return null;
            }

            return scaleSize(
                    options.outWidth,
                    options.outHeight,
                    maxWidth,
                    maxHeight);
        }
    }

    private boolean isPdf(final Resource resource)
    {
        return SCHEME_FILE.equals(resource.uri().getScheme())
                && resource.name().ext().equalsIgnoreCase("pdf"); // TODO
    }

    private final class DecodePdf extends DecodeBase
    {
        DecodePdf(
                final Object key,
                final ImageView view,
                final Resource resource)
        {
            super(key, view, resource);
        }

        @Override
        protected Bitmap decode() throws Exception
        {
            final File file = new File(resource.uri());
            try (final ParcelFileDescriptor fd = open(file, MODE_READ_ONLY);
                 final PdfRenderer renderer = new PdfRenderer(fd))
            {
                if (renderer.getPageCount() > 0)
                {
                    try (final PdfRenderer.Page page = renderer.openPage(0))
                    {
                        return render(page);
                    }
                }
            }
            return null;
        }

        private Bitmap render(final PdfRenderer.Page page)
        {
            final ScaledSize size = sizeOf(page);
            publishProgress(size);

            final Bitmap bitmap = bitmap(size);
            page.render(bitmap, null, null, RENDER_MODE_FOR_DISPLAY);
            return bitmap;
        }

        private ScaledSize sizeOf(final PdfRenderer.Page page)
        {
            final int width = pointToPixel(page.getWidth());
            final int height = pointToPixel(page.getHeight());
            return scaleSize(width, height, maxWidth, maxHeight);
        }

        private int pointToPixel(final int points)
        {
            final DisplayMetrics m = view.getResources().getDisplayMetrics();
            return (int) applyDimension(COMPLEX_UNIT_PT, points, m);
        }

        private Bitmap bitmap(final ScaledSize size)
        {
            final Bitmap bitmap = createBitmap(
                    size.scaledWidth, size.scaledHeight, ARGB_8888);
            // Without this PDFs with no background color will have window
            // background shown through
            bitmap.eraseColor(WHITE);
            return bitmap;
        }
    }

}
