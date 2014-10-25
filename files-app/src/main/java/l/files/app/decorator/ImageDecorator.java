package l.files.app.decorator;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;

import com.google.common.base.Optional;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import l.files.R;
import l.files.ui.analytics.Analytics;
import l.files.app.decorator.decoration.Decoration;
import l.files.app.util.ScaledSize;
import l.files.common.graphics.drawable.SizedColorDrawable;
import l.files.logging.Logger;

import static android.graphics.Color.TRANSPARENT;
import static android.os.AsyncTask.SERIAL_EXECUTOR;
import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static l.files.app.util.Bitmaps.decodeScaledBitmap;
import static l.files.app.util.Bitmaps.scaleSize;
import static l.files.provider.MediaContract.Bounds;
import static l.files.provider.MediaContract.decodeBounds;

final class ImageDecorator implements Decorator {

  private static final Logger logger = Logger.get(ImageDecorator.class);

  private static final Set<Object> errors = newHashSet();
  private static final Map<Object, ScaledSize> sizes = newHashMap();

  private final Decoration<String> fileIds;
  private final Decoration<Uri> uris;
  private final Decoration<Boolean> predicate;
  private final LruCache<Object, Bitmap> cache;
  private final int maxWidth;
  private final int maxHeight;

  ImageDecorator(
      Decoration<String> fileIds,
      Decoration<Uri> uris,
      Decoration<Boolean> predicate,
      LruCache<Object, Bitmap> cache,
      int maxWidth,
      int maxHeight) {
    checkArgument(maxWidth > 0);
    checkArgument(maxHeight > 0);
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
    this.fileIds = checkNotNull(fileIds, "fileIds");
    this.uris = checkNotNull(uris, "uris");
    this.predicate = checkNotNull(predicate, "predicate");
    this.cache = checkNotNull(cache, "cache");
  }

  @Override public void decorate(int position, Adapter adapter, View view) {
    Uri uri = uris.get(position, adapter);
    Object key = buildCacheKey(uri);

    Task task = (Task) view.getTag(R.id.image_decorator_task);
    if (task != null && task.uri().equals(uri)) return;
    if (task != null) task.cancel(true);

    ImageView image = (ImageView) view;
    image.setImageDrawable(null);
    image.setVisibility(GONE);
    image.setTag(R.id.image_decorator_task, null);

    if (!predicate.get(position, adapter)) return;
    if (errors.contains(key)) return;
    if (setCachedBitmap(image, key)) return;

    ScaledSize size = sizes.get(key);
    if (size != null) {
      image.setVisibility(VISIBLE);
      image.setImageDrawable(newPlaceholder(size));
      new DecodeImage(key, image, uri, size).executeOnExecutor(SERIAL_EXECUTOR);
    } else {
      new DecodeSize(key, image, uri, fileIds.get(position, adapter)).executeOnExecutor(THREAD_POOL_EXECUTOR);
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

  private Object buildCacheKey(Uri uri) {
    return uri.buildUpon()
        .appendQueryParameter("bounds", maxWidth + "x" + maxHeight)
        .build();
  }

  private SizedColorDrawable newPlaceholder(ScaledSize size) {
    return new SizedColorDrawable(TRANSPARENT, size.scaledWidth, size.scaledHeight);
  }

  private static interface Task {

    Uri uri();

    boolean cancel(boolean mayInterruptIfRunning);
  }

  private final class DecodeSize extends AsyncTask<Void, Void, ScaledSize> implements Task {
    private final Object key;
    private final ImageView view;
    private final Uri uri;
    private final String fileId;

    DecodeSize(Object key, ImageView view, Uri uri, String fileId) {
      this.uri = uri;
      this.key = key;
      this.view = view;
      this.fileId = fileId;
    }

    @Override protected void onPreExecute() {
      super.onPreExecute();
      view.setTag(R.id.image_decorator_task, this);
    }

    @Override protected ScaledSize doInBackground(Void... params) {
      if (isCancelled()) {
        return null;
      }
      ScaledSize size = null;
      Optional<Bounds> optional = decodeBounds(view.getContext(), fileId);
      if (optional.isPresent()) {
        Bounds bounds = optional.get();
        size = scaleSize(bounds.width(), bounds.height(), maxWidth, maxHeight);
      }
      return size;
    }

    @Override protected void onPostExecute(ScaledSize size) {
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
          new DecodeImage(key, view, uri, size).executeOnExecutor(SERIAL_EXECUTOR);
        }
      }
    }

    @Override public Uri uri() {
      return uri;
    }
  }

  private final class DecodeImage extends AsyncTask<Void, Void, Bitmap> implements Task {
    private final Object key;
    private final ImageView view;
    private final Uri uri;
    private final ScaledSize size;

    DecodeImage(Object key, ImageView view, Uri uri, ScaledSize size) {
      this.key = key;
      this.view = view;
      this.uri = uri;
      this.size = size;
    }

    @Override protected void onPreExecute() {
      super.onPreExecute();
      view.setTag(R.id.image_decorator_task, this);
    }

    @Override protected Bitmap doInBackground(Void... params) {
      if (isCancelled()) {
        return null;
      }
      try {
        return decodeScaledBitmap(new URI(uri.toString()).toURL(), size);
      } catch (Exception e) {
        // Catch all unexpected internal errors from decoder
        Analytics.onException(view.getContext(), e);
        logger.warn(e);
        return null;
      }
    }

    @Override protected void onPostExecute(Bitmap bitmap) {
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

    @Override public Uri uri() {
      return uri;
    }
  }
}
