package l.files.ui.widget;

import android.widget.ListAdapter;

import java.util.Collection;

public interface UpdatableAdapter<T> extends ListAdapter {

  void replaceAll(Collection<? extends T> items);

}
