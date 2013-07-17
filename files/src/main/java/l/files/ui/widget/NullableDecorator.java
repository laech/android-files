package l.files.ui.widget;

import android.view.View;

import static com.google.common.base.Preconditions.checkNotNull;

final class NullableDecorator<T> implements Decorator<T> {

  private final Decorator<T> delegate;
  private final int nullableViewId;

  NullableDecorator(int nullableViewId, Decorator<T> delegate) {
    this.delegate = checkNotNull(delegate, "delegate");
    this.nullableViewId = nullableViewId;
  }

  @Override public void decorate(View view, T item) {
    if (view.findViewById(nullableViewId) != null)
      delegate.decorate(view, item);
  }
}
