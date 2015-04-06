package l.files.common.widget;

import android.util.SparseBooleanArray;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

public final class ListViews {
  private ListViews() {}

  @SuppressWarnings("unchecked")
  public static <T> List<T> getCheckedItems(AbsListView list, Class<T> type) {
    SparseBooleanArray checks = list.getCheckedItemPositions();
    List<T> items = new ArrayList<>(checks.size());
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

  public static List<Integer> getCheckedItemPositions(AbsListView list) {
    SparseBooleanArray checks = list.getCheckedItemPositions();
    List<Integer> positions = new ArrayList<>(checks.size());
    for (int i = 0; i < checks.size(); i++) {
      if (checks.valueAt(i)) {
        positions.add(checks.keyAt(i));
      }
    }
    return positions;
  }

  public static int getCheckedItemPosition(AbsListView list) {
    List<Integer> positions = getCheckedItemPositions(list);
    if (positions.isEmpty()) {
      throw new IllegalArgumentException("No item checked.");
    }
    return positions.get(0);
  }
}
