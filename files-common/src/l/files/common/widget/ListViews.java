package l.files.common.widget;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

import android.util.SparseBooleanArray;
import android.widget.AbsListView;
import java.util.List;

public final class ListViews {
  private ListViews() {}

  @SuppressWarnings("unchecked")
  public static <T> List<T> getCheckedItems(AbsListView list, Class<T> type) {
    SparseBooleanArray checks = list.getCheckedItemPositions();
    List<T> items = newArrayListWithCapacity(checks.size());
    for (int i = 0; i < checks.size(); ++i) {
      if (!checks.valueAt(i)) {
        continue;
      }
      Object item = list.getItemAtPosition(checks.keyAt(i));
      if (type.isInstance(item)) {
        items.add((T) item);
      }
    }
    return items;
  }
}
