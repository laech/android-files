package l.files.common.widget;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import com.google.common.base.Function;

final class FontDecorator<T> implements Decorator<T> {

  private final int textViewId;
  private final Function<T, Typeface> typefaces;

  @SuppressWarnings("unchecked")
  FontDecorator(int textViewId, Function<? super T, ? extends Typeface> typefaces) {
    this.textViewId = textViewId;
    this.typefaces = (Function<T, Typeface>) typefaces;
  }

  @Override public void decorate(View view, T item) {
    ((TextView) view.findViewById(textViewId)).setTypeface(typefaces.apply(item));
  }
}
