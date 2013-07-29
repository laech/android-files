package l.files.ui.widget;

import android.graphics.drawable.Drawable;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

public final class Decorators {

  /**
   * Returns a decorator that is a composition of the given decorators, to be
   * executed in the given order.
   */
  public static <T> Decorator<T> compose(Decorator<? super T>... decorators) {
    return new CompositeDecorator<T>(decorators);
  }

  /**
   * Returns a decorator upon execution will set text to the text view with the
   * given view ID. The text is obtained by applying the given function to the
   * item.
   */
  public static <T> Decorator<T> text(
      int textViewId, Function<? super T, ? extends CharSequence> labels) {
    return new TextDecorator<T>(textViewId, labels);
  }

  /**
   * Returns a decorator upon execution that will set a left drawable to the
   * text view with the given view ID. The drawable is obtained by applying the
   * given function to the item.
   */
  public static <T> Decorator<T> draw(
      int textViewId, Function<? super T, ? extends Drawable> drawables) {
    return new DrawableDecorator<T>(textViewId, drawables);
  }

  /**
   * Returns a decorator upon execution will check the existence of a view
   * within hierarchy that has the given view ID, if found the delegates will be
   * executed.
   */
  public static <T> Decorator<T> nullable(int nullableViewId, Decorator<? super T>... delegates) {
    return new NullableDecorator<T>(nullableViewId, compose(delegates));
  }

  /**
   * Returns a decorator upon execution will enable/disable the view with the
   * given view ID depending on the result of applying the predicate to the
   * item.
   */
  public static <T> Decorator<T> enable(int viewId, Predicate<T> predicate) {
    return new EnableStateDecorator<T>(viewId, predicate);
  }

  private Decorators() {}

}
