package l.files.common.widget;

import android.view.View;

import static com.google.common.base.Preconditions.checkNotNull;

final class NullableDecorator<T> implements Decorator<T> {

  private final Decorator<T> delegate;
  private final int nullableViewId;

  @SuppressWarnings("unchecked")
  NullableDecorator(int nullableViewId, Decorator<? super T> delegate) {
    this.delegate = (Decorator<T>) checkNotNull(delegate, "delegate");
    this.nullableViewId = nullableViewId;
  }

  @Override public void decorate(View view, T item) {
    if (view.findViewById(nullableViewId) != null)
      delegate.decorate(view, item);
  }
}
