package l.files.ui.util;

import android.widget.AbsListView;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayListWithCapacity;

public final class ListViews {

  public static List<Object> getCheckedItems(AbsListView list) {
    int n = list.getCheckedItemCount();
    List<Object> items = newArrayListWithCapacity(n);
    for (int i = 0; i < list.getCount(); i++) {
      if (items.size() == n) break;
      if (list.isItemChecked(i)) items.add(list.getItemAtPosition(i));
    }
    return items;
  }

  public static <T> Iterable<T> getCheckedItems(AbsListView list, Class<T> type) {
    return filter(getCheckedItems(list), type);
  }

  private ListViews() {}

}
