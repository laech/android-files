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
import l.files.fs.ResourceStatus;
import l.files.logging.Logger;
import l.files.ui.util.ScaledSize;

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

final class ImageDecorator {

    private static final Logger logger = Logger.get(ImageDecorator.class);

    private static final Set<Object> errors = new HashSet<>();
    private static final Map<Object, ScaledSize> sizes = new HashMap<>();

    private final LruCache<Object, Bitmap> cache;
    private final int maxWidth;
    private final int maxHeight;

    ImageDecorator(LruCache<Object, Bitmap> cache, int maxWidth, int maxHeight) {
        checkArgument(maxWidth > 0);
        checkArgument(maxHeight > 0);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.cache = requireNonNull(cache, "cache");
    }

    public void decorate(ImageView view, ResourceStatus file) {
        Object key = buildCacheKey(file);

        Task task = (Task) view.getTag(R.id.image_decorator_task);
        if (task != null && task.getResource().equals(file.getResource()))
            return;
        if (task != null) task.cancel(true);

        view.setImageDrawable(null);
        view.setVisibility(GONE);
        view.setTag(R.id.image_decorator_task, null);

        if (!file.isReadable() || !file.isRegularFile()) return;
        if (errors.contains(key)) return;
        if (setCachedBitmap(view, key)) return;

        ScaledSize size = sizes.get(key);
        if (size != null) {
            view.setVisibility(VISIBLE);
            view.setImageDrawable(newPlaceholder(size));
            new DecodeImage(key, view, file, size).executeOnExecutor(SERIAL_EXECUTOR);
        } else {
            new DecodeSize(key, view, file).executeOnExecutor(THREAD_POOL_EXECUTOR);
        }
    }

    private boolean setCachedBitmap(ImageView image, Object key) {
        Bitmap bitmap = cache.get(key);
        if (bitmap != null) {
            image.setImageBitmap(bitmap);
            image.setVisibility(VISIBLE);
            return true;
        }
        return false;
    }

    private Object buildCacheKey(ResourceStatus file) {
        return file.getResource().getUri() + "?bounds=" + maxWidth + "x" + maxHeight;
    }

    private SizedColorDrawable newPlaceholder(ScaledSize size) {
        return new SizedColorDrawable(TRANSPARENT, size.scaledWidth, size.scaledHeight);
    }

    private static interface Task {

        Resource getResource();

        boolean cancel(boolean mayInterruptIfRunning);
    }

    private static InputStream openInputStream(Resource res) throws IOException {
        return res.openInputStream(FOLLOW);
    }

    private final class DecodeSize extends AsyncTask<Void, Void, ScaledSize> implements Task {
        private final Object key;
        private final ImageView view;
        private final ResourceStatus file;

        DecodeSize(Object key, ImageView view, ResourceStatus file) {
            this.key = key;
            this.view = view;
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            view.setTag(R.id.image_decorator_task, this);
        }

        @Override
        protected ScaledSize doInBackground(Void... params) {
            if (isCancelled()) {
                return null;
            }

      /* Some files are protected by more than just permission attributes, a file
       * with readable bits set doesn't mean it will be readable, they are also
       * being protected by other means, e.g. /proc/1/maps. Attempt to read those
       * files with BitmapFactory will cause the native code to catch and ignore
       * the exception and loop forever, see
       * https://github.com/android/platform_frameworks_base/blob/master/core/jni/android/graphics/CreateJavaOutputStreamAdaptor.cpp
       * This code here is a workaround for that, attempt to read one byte off the
       * file, if an exception occurs, meaning it can't be read, let the exception
       * propagate, and return no result.
       */
            try (InputStream in = openInputStream(file.getResource())) {
                //noinspection ResultOfMethodCallIgnored
                in.read();
            } catch (IOException e) {
                return null;
            }

            Options options = new Options();
            options.inJustDecodeBounds = true;
            try (InputStream in = openInputStream(file.getResource())) {
                decodeStream(in, null, options);
            } catch (IOException e) {
                return null;
            }
            if (options.outWidth > 0 && options.outHeight > 0) {
                return scaleSize(options.outWidth, options.outHeight, maxWidth, maxHeight);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ScaledSize size) {
            super.onPostExecute(size);
            if (size == null) {
                errors.add(key);
                if (view.getTag(R.id.image_decorator_task) == this) {
                    view.setTag(R.id.image_decorator_task, null);
                }
            } else {
                sizes.put(key, size);
                if (view.getTag(R.id.image_decorator_task) == this) {
                    view.setTag(R.id.image_decorator_task, null);
                    view.setImageDrawable(newPlaceholder(size));
                    view.setVisibility(VISIBLE);
                    new DecodeImage(key, view, file, size).executeOnExecutor(SERIAL_EXECUTOR);
                }
            }
        }

        @Override
        public Resource getResource() {
            return file.getResource();
        }
    }

    private final class DecodeImage extends AsyncTask<Void, Void, Bitmap> implements Task {
        private final Object key;
        private final ImageView view;
        private final ResourceStatus file;
        private final ScaledSize size;

        DecodeImage(Object key, ImageView view, ResourceStatus file, ScaledSize size) {
            this.key = key;
            this.view = view;
            this.file = file;
            this.size = size;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            view.setTag(R.id.image_decorator_task, this);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            if (isCancelled()) {
                return null;
            }
            try (InputStream in = openInputStream(file.getResource())) {
                return decodeScaledBitmap(in, size);
            } catch (Exception e) {
                // Catch all unexpected internal errors from decoder
                logger.warn(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap == null) {
                onFailed();
            } else {
                onSuccess(bitmap);
            }
        }

        private void onFailed() {
            errors.add(key);
            sizes.remove(key);
            if (view.getTag(R.id.image_decorator_task) == this) {
                view.setVisibility(GONE);
                view.setTag(R.id.image_decorator_task, null);
            }
        }

        private void onSuccess(Bitmap bitmap) {
            if (view.getTag(R.id.image_decorator_task) == this) {
                view.setTag(R.id.image_decorator_task, null);
                Resources res = view.getResources();
                TransitionDrawable drawable = new TransitionDrawable(new Drawable[]{
                        new ColorDrawable(TRANSPARENT),
                        new BitmapDrawable(res, bitmap)});
                view.setImageDrawable(drawable);
                int duration = res.getInteger(android.R.integer.config_shortAnimTime);
                drawable.startTransition(duration);
                view.setVisibility(VISIBLE);
            }
            cache.put(key, bitmap);
        }

        @Override
        public Resource getResource() {
            return file.getResource();
        }
    }
}
