package l.files.app.decorator;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.util.LruCache;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;

import java.util.Map;
import java.util.Set;

import l.files.R;
import l.files.app.decorator.decoration.Decoration;
import l.files.app.util.DecodeImageTask;
import l.files.app.util.ScaledSize;
import l.files.common.graphics.drawable.SizedColorDrawable;

import static android.graphics.Color.TRANSPARENT;
import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

final class ImageDecorator extends BaseDecorator<Uri> {

  private static final Set<Object> errors = newHashSet();
  private static final Map<Object, ScaledSize> sizes = newHashMap();

  private final LruCache<Object, Bitmap> cache;
  private final int maxWidth;
  private final int maxHeight;

  ImageDecorator(
      Decoration<? extends Uri> decoration,
      int maxWidth,
      int maxHeight,
      LruCache<Object, Bitmap> cache) {
    super(decoration);
    checkArgument(maxWidth > 0);
    checkArgument(maxHeight > 0);
    checkNotNull(cache);
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
    this.cache = cache;
  }

  @Override public void decorate(int position, Adapter adapter, View view) {
    ImageView image = (ImageView) view;
    image.setImageDrawable(null);
    image.setVisibility(GONE);

    Object key = buildCacheKey(decoration().get(position, adapter));
    if (errors.contains(key)) {
      return;
    }
    if (setCachedBitmap(image, key)) {
      return;
    }
    setPlaceholderSize(image, key);
    executeTaskIfNeeded(image, key, position, adapter);
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

  private void setPlaceholderSize(ImageView view, Object key) {
    ScaledSize size = sizes.get(key);
    if (size != null) {
      view.setImageDrawable(newPlaceholder(size));
      view.setVisibility(VISIBLE);
    }
  }

  private void executeTaskIfNeeded(ImageView image, Object key, int position, Adapter adapter) {
    DecodeTask task = (DecodeTask) image.getTag(R.id.image_decorator_task);
    if (!key.equals(image.getTag(R.id.image_decorator_key))) {
      if (task != null) {
        task.cancel(true);
      }
      task = null;
    }

    if (task == null) {
      task = new DecodeTask(key, image);
      image.setTag(R.id.image_decorator_task, task);
      image.setTag(R.id.image_decorator_key, key);
      task.executeOnExecutor(THREAD_POOL_EXECUTOR,
          decoration().get(position, adapter).toString());
    }
  }

  private Object buildCacheKey(Uri uri) {
    return uri.buildUpon()
        .appendQueryParameter("bounds", maxWidth + "x" + maxHeight)
        .build();
  }

  private SizedColorDrawable newPlaceholder(ScaledSize size) {
    return new SizedColorDrawable(TRANSPARENT, size.scaledWidth, size.scaledHeight);
  }

  // TODO no need to decode size again
  // TODO check file is readable before decoding, currently crashes
  private final class DecodeTask extends DecodeImageTask {
    private final Object key;
    private final ImageView view;

    DecodeTask(Object key, ImageView view) {
      super(maxWidth, maxHeight);
      this.key = key;
      this.view = view;
    }

    @Override protected void onProgressUpdate(ScaledSize... values) {
      super.onProgressUpdate(values);
      ScaledSize size = values[0];
      sizes.put(key, size);
      if (isCurrentKey()) {
        view.setImageDrawable(newPlaceholder(size));
        view.setVisibility(VISIBLE);
      } else {
        cancel(true);
      }
    }

    @Override protected void onCancelled() {
      if (isCurrentKey()) {
        view.setTag(R.id.image_decorator_task, null);
        view.setTag(R.id.image_decorator_key, null);
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
      if (isCurrentKey()) {
        view.setVisibility(GONE);
        view.setTag(R.id.image_decorator_task, null);
        view.setTag(R.id.image_decorator_key, null);
      }
    }

    private void onSuccess(Bitmap bitmap) {
      if (isCurrentKey()) {
        Resources res = view.getResources();
        TransitionDrawable drawable = new TransitionDrawable(new Drawable[]{
            new ColorDrawable(TRANSPARENT),
            new BitmapDrawable(res, bitmap)});
        view.setImageDrawable(drawable);
        int duration = res.getInteger(android.R.integer.config_shortAnimTime);
        drawable.startTransition(duration);
        view.setVisibility(VISIBLE);
        view.setTag(R.id.image_decorator_task, null);
        view.setTag(R.id.image_decorator_key, null);
      }
      cache.put(key, bitmap);
    }

    private boolean isCurrentKey() {
      return key.equals(view.getTag(R.id.image_decorator_key));
    }
  }
}
