package l.files.ui.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;

final class LabelViewer<T> implements Viewer<T> {

  private final Function<T, CharSequence> labels;
  private final int textViewId;

  @SuppressWarnings("unchecked") LabelViewer(
      int textViewId, Function<? super T, ? extends CharSequence> labels) {

    this.labels = checkNotNull((Function<T, CharSequence>) labels, "labels");
    this.textViewId = textViewId;
  }

  @Override public View getView(T item, View view, ViewGroup parent) {
    CharSequence text = labels.apply(item);
    TextView textView = (TextView) view.findViewById(textViewId);
    textView.setText(text);
    return view;
  }

}
