package l.files.ui.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

final class LayoutViewer<T> implements Viewer<T> {

  private final int resId;

  LayoutViewer(int resId) {
    this.resId = resId;
  }

  @Override public View getView(T item, View view, ViewGroup parent) {
    return view != null ? view : inflate(parent);
  }

  private View inflate(ViewGroup parent) {
    return LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
  }

}
