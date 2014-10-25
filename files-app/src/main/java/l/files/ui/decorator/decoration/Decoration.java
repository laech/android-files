package l.files.ui.decorator.decoration;

import android.widget.Adapter;

import l.files.ui.decorator.Decorator;

/**
 * An object that provides decoration values at a given position for {@link
 * Decorator}s.
 */
public interface Decoration<T> {

  /**
   * Returns the decoration at the given position.
   */
  T get(int position, Adapter adapter);
}
