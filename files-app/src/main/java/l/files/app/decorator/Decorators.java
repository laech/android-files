package l.files.app.decorator;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.LruCache;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;

import l.files.app.decorator.decoration.Decoration;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class Decorators {
  private Decorators() {}

  /**
   * Returns a composition of the given instances.
   */
  public static Decorator compose(final Decorator... decorators) {
    if (decorators.length == 1) {
      return decorators[0];
    }
    return new Decorator() {
      @Override public void decorate(int position, Adapter adapter, View view) {
        for (Decorator decorator : decorators) {
          decorator.decorate(position, adapter, view);
        }
      }
    };
  }

  /**
   * Returns a decorator to call {@code delegates} on the child view with the
   * given ID instead of the original view.
   */
  public static Decorator on(int childViewId, Decorator... delegates) {
    return on(childViewId, compose(delegates));
  }

  /**
   * Returns a decorator to call {@code delegate} on the child view with the
   * given ID instead of the original view.
   */
  public static Decorator on(final int childViewId, final Decorator delegate) {
    return new Decorator() {
      @Override public void decorate(int position, Adapter adapter, View view) {
        View child = (View) view.getTag(childViewId);
        if (child == null) {
          child = view.findViewById(childViewId);
          view.setTag(childViewId, child);
        }
        delegate.decorate(position, adapter, child);
      }
    };
  }

  /**
   * Returns a decorator that expects each view to be a {@link TextView} and
   * sets text provided by the decoration.
   */
  public static Decorator text(
      final Decoration<? extends CharSequence> decoration) {
    return new Decorator() {
      @Override public void decorate(int position, Adapter adapter, View view) {
        ((TextView) view).setText(decoration.get(position, adapter));
      }
    };
  }

  /**
   * Returns a decorator that will enable/disable the view using the values
   * provided by the decoration.
   */
  public static Decorator enable(
      final Decoration<? extends Boolean> decoration) {
    return new Decorator() {
      @Override public void decorate(int position, Adapter adapter, View view) {
        view.setEnabled(decoration.get(position, adapter));
      }
    };
  }

  /**
   * Returns a decorator that expects each view to be a {@link TextView} and set
   * its typeface using the values provided by the decoration.
   */
  public static Decorator font(
      final Decoration<? extends Typeface> decoration) {
    return new Decorator() {
      @Override public void decorate(int position, Adapter adapter, View view) {
        ((TextView) view).setTypeface(decoration.get(position, adapter));
      }
    };
  }

  /**
   * Returns a decorator that will set the visibility of each view to be {@link
   * View#VISIBLE} or {@link View#GONE} using the values provided by the
   * decoration.
   */
  public static Decorator visible(
      final Decoration<? extends Boolean> decoration) {
    return new Decorator() {
      @Override public void decorate(int position, Adapter adapter, View view) {
        view.setVisibility(decoration.get(position, adapter) ? VISIBLE : GONE);
      }
    };
  }

  /**
   * Returns a decorator that expects each view to be an {@link ImageView} and
   * sets its image by decoding a thumbnail of the {@link Uri}s provided by the
   * {@code uris}. If failed to decode a thumbnail, or the predicate returns
   * false for a given position, the view will be hidden. The maximum width and
   * height parameters defines how large the thumbnails are allowed to be (large
   * thumbnails will be scaled down to fit).
   */
  public static Decorator image(
      Decoration<String> fileIds,
      Decoration<Uri> uris,
      Decoration<Boolean> predicate,
      LruCache<Object, Bitmap> cache,
      int maxWidth,
      int maxHeight) {
    return new ImageDecorator(fileIds, uris, predicate, cache, maxWidth, maxHeight);
  }
}
