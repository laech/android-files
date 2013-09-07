package l.files.common.widget;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.util.Collections.unmodifiableList;

import android.util.SparseBooleanArray;
import android.widget.AbsListView;
import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.List;

public final class ListViews {
  private ListViews() {}

  public static List<Object> getItems(AbsListView list) {
    return getItems(list, alwaysTrue());
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> getItems(AbsListView list, Class<T> clazz) {
    return (List<T>) getItems(list, instanceOf(clazz));
  }

  @SuppressWarnings("unchecked")
  public static List<Object> getItems(AbsListView list, Predicate<?> predicate) {
    int count = list.getCount();
    ArrayList<Object> items = newArrayListWithCapacity(count);
    for (int i = 0; i < count; i++) {
      Object item = list.getItemAtPosition(i);
      if (((Predicate<Object>) predicate).apply(item)) {
        items.add(item);
      }
    }
    items.trimToSize();
    return unmodifiableList(items);
  }

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
