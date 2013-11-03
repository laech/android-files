package l.files.common.widget;

import static com.google.common.base.Preconditions.checkNotNull;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Predicate;

final class EnableStateDecorator<T> implements Decorator<T> {

  private final Predicate<T> pred;

  EnableStateDecorator(Predicate<T> pred) {
    this.pred = checkNotNull(pred, "pred");
  }

  @Override public void decorate(View view, T item) {
    enable(pred.apply(item), view);
  }

  private void enable(boolean enabled, View view) {
    view.setEnabled(enabled);
    if (view instanceof ViewGroup) {
      ViewGroup parent = (ViewGroup) view;
      for (int i = 0; i < parent.getChildCount(); i++) {
        enable(enabled, parent.getChildAt(i));
      }
    }
  }
}
