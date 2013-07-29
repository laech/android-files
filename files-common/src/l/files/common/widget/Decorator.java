package l.files.common.widget;

import android.view.View;

/**
 * Decorates a view with extra properties from an item.
 *
 * @param <T> the item type
 */
public interface Decorator<T> {
  /**
   * Decorates the view with the item.
   *
   * @param view the view to be decorated
   * @param item the model of this view
   */
  void decorate(View view, T item);
}
