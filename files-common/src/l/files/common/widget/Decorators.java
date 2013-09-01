package l.files.common.widget;

import android.graphics.Typeface;
import android.widget.TextView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

public final class Decorators {
  private Decorators() {}

  /**
   * Returns a decorator that is a composition of the given decorators, to be
   * executed in the given order.
   */
  public static <T> Decorator<T> compose(final Decorator<? super T>... decorators) {
    return new CompositeDecorator<T>(decorators);
  }

  /**
   * Creates a decorator will will find a specific view with the given ID and
   * applies the decorator on it.
   */
  public static <T> Decorator<T> on(final int id, final Decorator<? super T> decorator) {
    return new OnDecorator<T>(id, decorator);
  }

  /**
   * Creates a decorator that will only accept {@link TextView}s, and sets the
   * view's text to that retrieved by applying the function to the model item.
   */
  public static <T> Decorator<T> text(final Function<? super T, ? extends CharSequence> labels) {
    return new TextDecorator<T>(labels);
  }

  /**
   * Returns a decorator upon execution will enable/disable the view tree with
   * the result of applying the predicate to the item.
   */
  public static <T> Decorator<T> enable(final Predicate<T> predicate) {
    return new EnableStateDecorator<T>(predicate);
  }

  /**
   * Creates a decorator that will only accept {@link TextView}s, and sets the
   * view's font to that retrieved by applying the function to the model item.
   */
  public static <T> Decorator<T> font(final Function<? super T, ? extends Typeface> fonts) {
    return new FontDecorator<T>(fonts);
  }
}
