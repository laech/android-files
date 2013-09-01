package l.files.common.widget;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import com.google.common.base.Function;

final class FontDecorator<T> implements Decorator<T> {

  private final Function<T, Typeface> typefaces;

  @SuppressWarnings("unchecked")
  FontDecorator(Function<? super T, ? extends Typeface> typefaces) {
    this.typefaces = (Function<T, Typeface>) typefaces;
  }

  @Override public void decorate(View view, T item) {
    ((TextView) view).setTypeface(typefaces.apply(item));
  }
}
