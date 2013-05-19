package l.files.ui.util;

import android.view.View;
import android.widget.ListView;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.util.Collections.emptyList;

public final class ListViews {

  private ListViews() {
  }

  public static List<Object> getCheckedItems(ListView list) {
    int n = list.getCheckedItemCount();
    List<Object> items = newArrayListWithCapacity(n);
    for (int i = 0; i < list.getCount(); i++) {
      if (items.size() == n) break;
      if (list.isItemChecked(i)) items.add(list.getItemAtPosition(i));
    }
    return items;
  }

  public static <T> Iterable<T> getCheckedItems(ListView list, Class<T> type) {
    return filter(getCheckedItems(list), type);
  }

  public static List<Object> getVisibleItems(ListView listView) {
    int count = listView.getChildCount();
    if (0 == count) return emptyList();

    List<Object> visibleItems = newArrayListWithCapacity(count);
    int first = listView.getFirstVisiblePosition();
    int last = listView.getLastVisiblePosition();
    for (int i = first; i <= last; i++)
      visibleItems.add(listView.getItemAtPosition(i));

    return visibleItems;
  }

  public static int getFirstChildScrollOffset(ListView listView) {
    View view = listView.getChildAt(0);
    return view == null ? 0 : view.getTop();
  }
}
