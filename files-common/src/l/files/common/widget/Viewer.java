package l.files.common.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

public interface Viewer<T> {

  /**
   * @see Adapter#getView(int, View, ViewGroup)
   */
  View getView(T item, View view, ViewGroup parent);
}
