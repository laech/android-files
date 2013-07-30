package l.files.common.widget;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;

final class DrawableDecorator<T> implements Decorator<T> {
  private final int textViewId;
  private final Function<T, Drawable> drawables;

  @SuppressWarnings("unchecked") DrawableDecorator(
      int textViewId, Function<? super T, ? extends Drawable> drawables) {
    this.drawables = checkNotNull((Function<T, Drawable>) drawables, "drawables");
    this.textViewId = textViewId;
  }

  @Override public void decorate(View view, T item) {
    Drawable img = drawables.apply(item);
    TextView textView = (TextView) view.findViewById(textViewId);
    textView.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
  }
}
