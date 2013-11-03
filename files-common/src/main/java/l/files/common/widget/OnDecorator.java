package l.files.common.widget;

import static com.google.common.base.Preconditions.checkNotNull;

import android.view.View;

final class OnDecorator<T> implements Decorator<T> {

  private final int id;
  private final Decorator<T> delegate;

  @SuppressWarnings("unchecked")
  OnDecorator(int id, Decorator<? super T> delegate) {
    this.id = id;
    this.delegate = (Decorator<T>) checkNotNull(delegate);
  }

  @Override public void decorate(View view, T item) {
    delegate.decorate(view.findViewById(id), item);
  }
}
