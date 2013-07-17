package l.files.ui.widget;

import android.view.View;

public interface Decorator<T> {

  void decorate(View view, T item);

}
