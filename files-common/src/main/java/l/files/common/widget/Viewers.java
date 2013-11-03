package l.files.common.widget;

/**
 * Static utility methods pertaining to {@code Viewer} instances.
 */
public final class Viewers {
  private Viewers() {}

  /**
   * Returns a viewer that will inflate layout if needed but will delegate the
   * decoration of the inflated view to a list of decorators.
   *
   * @param layoutResId the resource ID of the layout file
   * @param decorators the decorators to decorate the view
   */
  public static <T> Viewer<T> decorate(
      int layoutResId, Decorator<? super T>... decorators) {
    return new DecorationViewer<T>(layoutResId, decorators);
  }
}
