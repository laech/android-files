package l.files.common.widget;

import android.view.View;

import static com.google.common.base.Preconditions.checkNotNull;

final class CompositeDecorator<T> implements Decorator<T> {

  private final Decorator<T>[] decorators;

  @SuppressWarnings("unchecked")
  CompositeDecorator(Decorator<? super T>... decorators) {
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
