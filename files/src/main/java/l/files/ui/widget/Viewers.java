package l.files.ui.widget;

import android.graphics.drawable.Drawable;
import com.google.common.base.Function;

public final class Viewers {

  public static <T> Viewer<T> compose(Viewer<? super T>... viewers) {
    return new CompositeViewer<T>(viewers);
  }

  public static <T> Viewer<T> layout(int resId) {
    return new LayoutViewer<T>(resId);
  }

  public static <T> Viewer<T> text(
      int textViewId, Function<? super T, ? extends CharSequence> labels) {
    return new LabelViewer<T>(textViewId, labels);
  }

  public static <T> Viewer<T> draw(
      int textViewId, Function<? super T, ? extends Drawable> drawables) {
    return new DrawableViewer<T>(textViewId, drawables);
  }

  private Viewers() {}

}
