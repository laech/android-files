package l.files.ui.widget;

import android.view.View;
import android.view.ViewGroup;
import com.google.common.base.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

final class EnableStateViewer<T> implements Viewer<T> {

  private final int viewId;
  private final Predicate<T> predicate;

  EnableStateViewer(int viewId, Predicate<T> predicate) {
    this.viewId = viewId;
    this.predicate = checkNotNull(predicate, "predicate");
  }

  @Override public View getView(T item, View view, ViewGroup parent) {
    view.findViewById(viewId).setEnabled(predicate.apply(item));
    return view;
  }

}
