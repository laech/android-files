package l.files.ui.widget;

public final class Viewers {

  public static <T> Viewer<T> decorate(int layoutResId, Decorator<? super T>... decorators) {
    return new DecorationViewer<T>(layoutResId, decorators);
  }

  private Viewers() {}

}
