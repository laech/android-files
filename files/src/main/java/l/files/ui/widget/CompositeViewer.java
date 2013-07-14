package l.files.ui.widget;

import android.view.View;
import android.view.ViewGroup;

import static com.google.common.base.Preconditions.checkNotNull;

final class CompositeViewer<T> implements Viewer<T> {

  private final Viewer<T>[] viewers;

  @SuppressWarnings("unchecked") CompositeViewer(Viewer<? super T>... viewers) {
    this.viewers = (Viewer<T>[]) checkNotNull(viewers, "viewers").clone();
  }

  @Override public View getView(T item, View view, ViewGroup parent) {
    for (Viewer<T> viewer : viewers) view = viewer.getView(item, view, parent);
    return view;
  }

}
