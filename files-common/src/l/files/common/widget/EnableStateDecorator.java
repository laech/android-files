package l.files.common.widget;

import android.view.View;
import com.google.common.base.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

final class EnableStateDecorator<T> implements Decorator<T> {

  private final int viewId;
  private final Predicate<T> predicate;

  EnableStateDecorator(int viewId, Predicate<T> predicate) {
    this.viewId = viewId;
    this.predicate = checkNotNull(predicate, "predicate");
  }

  @Override public void decorate(View view, T item) {
    view.findViewById(viewId).setEnabled(predicate.apply(item));
  }
}
