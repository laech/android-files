package l.files.common.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Static utility methods pertaining to {@code Viewer} instances.
 */
public final class Viewers {
  private Viewers() {}

  /**
   * Returns a viewer that will inflate layout if needed but will delegate the
   * decoration of the inflated view to a list of decorators.
   *
   * @param layoutResId the resource ID of the layout file
   * @param decorators the decorators to decorate the view
   */
  public static <T> Viewer<T> decorate(
      int layoutResId, Decorator<? super T>... decorators) {
    return new DecorationViewer<T>(layoutResId, decorators);
  }

  private static class DecorationViewer<T> implements Viewer<T> {
    private final int layoutResId;
    private final Decorator<T>[] decorators;

    @SuppressWarnings("unchecked")
    DecorationViewer(int layoutResId, Decorator<? super T>... decorators) {
      this.layoutResId = layoutResId;
      this.decorators = (Decorator<T>[]) checkNotNull(decorators, "decorators").clone();
      for (Decorator<?> decorator : decorators) {
        checkNotNull(decorator, "decorator");
      }
    }

    @Override public View getView(T item, View view, ViewGroup parent) {
      if (view == null) {
        view = inflate(parent);
      }
      for (Decorator<T> decorator : decorators) {
        decorator.decorate(view, item);
      }
      return view;
    }

    private View inflate(ViewGroup parent) {
      return LayoutInflater.from(parent.getContext())
          .inflate(layoutResId, parent, false);
    }
  }
}
