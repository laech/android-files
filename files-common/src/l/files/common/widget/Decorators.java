package l.files.common.widget;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Static utility methods pertaining to {@code Decorator} instances.
 */
public final class Decorators {
  private Decorators() {}

  /**
   * Returns a decorator that is a composition of the given decorators, to be
   * executed in the given order.
   */
  public static <T> Decorator<T> compose(Decorator<? super T>... decorators) {
    return new DecoratorComposition<T>(decorators);
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
  public static <T> Decorator<T> nullable(
      int nullableViewId, Decorator<? super T>... delegates) {
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

  private static class DecoratorComposition<T> implements Decorator<T> {
    private final Decorator<T>[] decorators;

    @SuppressWarnings("unchecked")
    DecoratorComposition(Decorator<? super T>... decorators) {
      this.decorators = (Decorator<T>[]) checkNotNull(decorators, "decorators").clone();
      for (Decorator<?> decorator : decorators) {
        checkNotNull(decorator, "decorator");
      }
    }

    @Override public void decorate(View view, T item) {
      for (Decorator<T> decorator : decorators) {
        decorator.decorate(view, item);
      }
    }
  }

  private static class TextDecorator<T> implements Decorator<T> {
    private final Function<T, CharSequence> labels;
    private final int textViewId;

    @SuppressWarnings("unchecked") TextDecorator(
        int textViewId, Function<? super T, ? extends CharSequence> labels) {
      this.labels = checkNotNull((Function<T, CharSequence>) labels, "labels");
      this.textViewId = textViewId;
    }

    @Override public void decorate(View view, T item) {
      CharSequence text = labels.apply(item);
      TextView textView = (TextView) view.findViewById(textViewId);
      textView.setText(text);
    }
  }

  private static class DrawableDecorator<T> implements Decorator<T> {
    private final int textViewId;
    private final Function<T, Drawable> drawables;

    @SuppressWarnings("unchecked") DrawableDecorator(
        int textViewId, Function<? super T, ? extends Drawable> drawables) {
      this.drawables = checkNotNull((Function<T, Drawable>) drawables, "drawables");
      this.textViewId = textViewId;
    }

    @Override public void decorate(View view, T item) {
      Drawable img = drawables.apply(item);
      TextView textView = (TextView) view.findViewById(textViewId);
      textView.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    }
  }

  private static class NullableDecorator<T> implements Decorator<T> {
    private final Decorator<T> delegate;
    private final int nullableViewId;

    @SuppressWarnings("unchecked")
    NullableDecorator(int nullableViewId, Decorator<? super T> delegate) {
      this.delegate = (Decorator<T>) checkNotNull(delegate, "delegate");
      this.nullableViewId = nullableViewId;
    }

    @Override public void decorate(View view, T item) {
      if (view.findViewById(nullableViewId) != null) {
        delegate.decorate(view, item);
      }
    }
  }

  private static class EnableStateDecorator<T> implements Decorator<T> {
    private final int viewId;
    private final Predicate<T> predicate;

    EnableStateDecorator(int viewId, Predicate<T> predicate) {
      this.predicate = checkNotNull(predicate, "predicate");
      this.viewId = viewId;
    }

    @Override public void decorate(View view, T item) {
      view.findViewById(viewId).setEnabled(predicate.apply(item));
    }
  }
}
