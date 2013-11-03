package l.files.common.widget;

import android.view.View;
import android.widget.TextView;
import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;

final class TextDecorator<T> implements Decorator<T> {
  private final Function<T, CharSequence> labels;

  @SuppressWarnings("unchecked")
  TextDecorator(Function<? super T, ? extends CharSequence> labels) {
    this.labels = checkNotNull((Function<T, CharSequence>) labels, "labels");
  }

  @Override public void decorate(View view, T item) {
    ((TextView) view).setText(labels.apply(item));
  }
}
