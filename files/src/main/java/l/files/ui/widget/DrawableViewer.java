package l.files.ui.widget;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;

final class DrawableViewer<T> implements Viewer<T> {

  private final int textViewId;
  private final Function<T, Drawable> drawables;

  @SuppressWarnings("unchecked") DrawableViewer(
      int textViewId, Function<? super T, ? extends Drawable> drawables) {

    this.drawables = checkNotNull((Function<T, Drawable>) drawables, "drawables");
    this.textViewId = textViewId;
  }

  @Override public View getView(T item, View view, ViewGroup parent) {
    Drawable img = drawables.apply(item);
    TextView textView = (TextView) view.findViewById(textViewId);
    textView.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    return view;
  }

}
