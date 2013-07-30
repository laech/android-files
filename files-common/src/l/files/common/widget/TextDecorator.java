package l.files.common.widget;

import android.view.View;
import android.widget.TextView;
import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;

final class TextDecorator<T> implements Decorator<T> {
  private final Function<T, CharSequence> labels;
  private final int textViewId;

  @SuppressWarnings("unchecked") TextDecorator(
      int textViewId, Function<? super T, ? extends CharSequence> labels) {
    this.labels = checkNotNull((Function<T, CharSequence>) labels, "labels");
    this.textViewId = textViewId;
  }

  @Override public void decorate(View view, T item) {
    CharSequence text = labels.apply(item);
    TextView textView = (TextView) view.findViewById(textViewId);
    textView.setText(text);
  }
}
