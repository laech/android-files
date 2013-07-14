package l.files.ui.widget;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

public final class Viewers {

  /**
   * Returns a viewer that is a composition of the given viewers, to be executed
   * in the given order.
   */
  public static <T> Viewer<T> compose(Viewer<? super T>... viewers) {
    return new CompositeViewer<T>(viewers);
  }

  /**
   * Returns a viewer upon execution will check whether the view argument is
   * null, if the view is null, it will inflate a new view with the given layout
   * resource ID, if the view is not null, then the view is simply returned.
   */
  public static <T> Viewer<T> layout(int layoutResId) {
    return new LayoutViewer<T>(layoutResId);
  }

  /**
   * Returns a viewer upon execution will set text to the text view with the
   * given view ID. The text is obtained by applying the given function to the
   * item.
   */
  public static <T> Viewer<T> text(
      int textViewId, Function<? super T, ? extends CharSequence> labels) {
    return new LabelViewer<T>(textViewId, labels);
  }

  /**
   * Returns a viewer upon execution that will set a left drawable to the text
   * view with the given view ID. The drawable is obtained by applying the given
   * function to the item.
   */
  public static <T> Viewer<T> draw(
      int textViewId, Function<? super T, ? extends Drawable> drawables) {
    return new DrawableViewer<T>(textViewId, drawables);
  }

  /**
   * Returns a viewer upon execution will check the existence of a view within
   * hierarchy that has the given view ID, if found then the result of executing
   * the delegate will be returned, otherwise the original view passed to {@link
   * Viewer#getView(Object, View, ViewGroup)} will be returned without calling
   * the delegate.
   */
  public static <T> Viewer<T> nullable(int nullableViewId, Viewer<T> delegate) {
    return new NullableViewer<T>(nullableViewId, delegate);
  }

  /**
   * Returns a viewer upon execution will enable/disable the view with the given
   * view ID depending on the result of applying the predicate to the item.
   */
  public static <T> Viewer<T> enable(int viewId, Predicate<T> predicate) {
    return new EnableStateViewer<T>(viewId, predicate);
  }

  private Viewers() {}

}
