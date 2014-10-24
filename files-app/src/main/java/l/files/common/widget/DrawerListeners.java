package l.files.common.widget;

import static android.support.v4.widget.DrawerLayout.DrawerListener;

public final class DrawerListeners {
  private DrawerListeners() {}

  /**
   * Creates a {@link DrawerListener} that is composed of the given delegates,
   * when called will execute the delegates in turn.
   *
   * @throws NullPointerException if any {@code delegates} is null or contains null
   */
  public static DrawerListener compose(DrawerListener... delegates) {
    return new CompositeDrawerListener(delegates);
  }
}
