package l.files.common.widget;

public final class Viewers {
  private Viewers() {}

  public static <T> Viewer<T> decorate(int layoutResId, Decorator<? super T>... decorators) {
    return new DecorationViewer<T>(layoutResId, decorators);
  }
}
