package l.files.ui.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.google.common.base.Preconditions.checkNotNull;

final class DecorationViewer<T> implements Viewer<T> {

  private final int layoutResId;
  private final Decorator<T>[] decorators;

  @SuppressWarnings("unchecked")
  DecorationViewer(int layoutResId, Decorator<? super T>... decorators) {
    this.layoutResId = layoutResId;
    this.decorators = (Decorator<T>[]) checkNotNull(decorators, "decorators");
    for (Decorator<?> decorator : decorators) checkNotNull(decorator, "decorator");
  }

  @Override public View getView(T item, View view, ViewGroup parent) {
    if (view == null) view = inflate(parent);
    for (Decorator<T> decorator : decorators) decorator.decorate(view, item);
    return view;
  }

  private View inflate(ViewGroup parent) {
    return LayoutInflater.from(parent.getContext())
        .inflate(layoutResId, parent, false);
  }

}
