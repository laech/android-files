package l.files.app.decorator;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

/**
 * Object representation of a reusable function to be called within {@link
 * Adapter#getView(int, View, ViewGroup)}.
 */
public interface Decorator {

  /**
   * Decorates the view at the given position.
   */
  void decorate(int position, Adapter adapter, View view);
}
