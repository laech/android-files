package l.files.ui.browser;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import l.files.R;
import l.files.common.graphics.drawable.SizedColorDrawable;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.logging.Logger;
import l.files.ui.util.ScaledSize;

import static android.R.integer.config_shortAnimTime;
import static android.graphics.BitmapFactory.Options;
import static android.graphics.BitmapFactory.decodeStream;
import static android.graphics.Color.TRANSPARENT;
import static android.os.AsyncTask.SERIAL_EXECUTOR;
import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
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
        if (task != null && task.getResource().equals(resource)) return;
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
            new DecodeImage(key, view, resource, size)
                    .executeOnExecutor(SERIAL_EXECUTOR);
        }
        else
        {
            new DecodeSize(key, view, resource)
                    .executeOnExecutor(THREAD_POOL_EXECUTOR);
        }
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
                TRANSPARENT, size.scaledWidth, size.scaledHeight);
    }

    private interface Task
    {

        Resource getResource();

        boolean cancel(boolean mayInterruptIfRunning);
    }

    private static InputStream input(final Resource res) throws IOException
    {
        return res.input(FOLLOW);
    }

    private final class DecodeSize extends AsyncTask<Void, Void, ScaledSize>
            implements Task
    {
        private final Object key;
        private final ImageView view;
        private final Resource resource;

        DecodeSize(
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
        protected ScaledSize doInBackground(final Void... params)
        {
            if (isCancelled())
            {
                return null;
            }

            final Options options = new Options();
            options.inJustDecodeBounds = true;
            try (InputStream in = input(resource))
            {
                decodeStream(in, null, options);
            }
            catch (final IOException e)
            {
                return null;
            }
            if (options.outWidth > 0 && options.outHeight > 0)
            {
                return scaleSize(
                        options.outWidth,
                        options.outHeight,
                        maxWidth,
                        maxHeight);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ScaledSize size)
        {
            super.onPostExecute(size);
            if (size == null)
            {
                errors.add(key);
                if (view.getTag(R.id.image_decorator_task) == this)
                {
                    view.setTag(R.id.image_decorator_task, null);
                }
            }
            else
            {
                sizes.put(key, size);
                if (view.getTag(R.id.image_decorator_task) == this)
                {
                    view.setTag(R.id.image_decorator_task, null);
                    view.setImageDrawable(newPlaceholder(size));
                    view.setVisibility(VISIBLE);
                    new DecodeImage(key, view, resource, size)
                            .executeOnExecutor(SERIAL_EXECUTOR);
                }
            }
        }

        @Override
        public Resource getResource()
        {
            return resource;
        }
    }

    private final class DecodeImage extends AsyncTask<Void, Void, Bitmap>
            implements Task
    {
        private final Object key;
        private final ImageView view;
        private final Resource resource;
        private final ScaledSize size;

        DecodeImage(
                final Object key,
                final ImageView view,
                final Resource resource,
                final ScaledSize size)
        {
            this.key = key;
            this.view = view;
            this.resource = resource;
            this.size = size;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            view.setTag(R.id.image_decorator_task, this);
        }

        @Override
        protected Bitmap doInBackground(final Void... params)
        {
            if (isCancelled())
            {
                return null;
            }
            try (InputStream in = input(resource))
            {
                return decodeScaledBitmap(in, size);
            }
            catch (final Exception e)
            {
                // Catch all unexpected internal errors from decoder
                logger.warn(e);
                return null;
            }
        }

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
                                new BitmapDrawable(res, bitmap)});
                view.setImageDrawable(drawable);
                final int duration = res.getInteger(config_shortAnimTime);
                drawable.startTransition(duration);
                view.setVisibility(VISIBLE);
            }
            cache.put(key, bitmap);
        }

        @Override
        public Resource getResource()
        {
            return resource;
        }
    }
}
