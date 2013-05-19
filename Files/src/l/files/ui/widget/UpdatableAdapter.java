package l.files.ui.widget;

import android.widget.ListAdapter;

import java.util.Collection;
import java.util.Comparator;

public interface UpdatableAdapter<T> extends ListAdapter {

  void addAll(Collection<? extends T> collection, Comparator<? super T> comparator);

  void removeAll(Collection<?> collection);

  void replaceAll(Collection<? extends T> collection, Comparator<? super T> comparator);

}
