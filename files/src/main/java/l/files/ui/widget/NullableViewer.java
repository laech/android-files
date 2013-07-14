package l.files.ui.widget;

import android.view.View;
import android.view.ViewGroup;

import static com.google.common.base.Preconditions.checkNotNull;

final class NullableViewer<T> implements Viewer<T> {

  private final Viewer<T> delegate;
  private final int nullableViewId;

  NullableViewer(int nullableViewId, Viewer<T> delegate) {
    this.delegate = checkNotNull(delegate, "delegate");
    this.nullableViewId = nullableViewId;
  }

  @Override public View getView(T item, View view, ViewGroup parent) {
    return view.findViewById(nullableViewId) != null
        ? delegate.getView(item, view, parent)
        : view;
  }
}
