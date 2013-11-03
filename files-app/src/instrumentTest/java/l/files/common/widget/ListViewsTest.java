package l.files.common.widget;

import static com.google.common.base.Predicates.equalTo;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static l.files.common.widget.ListViews.getCheckedItems;
import static l.files.common.widget.ListViews.getItems;

import android.test.AndroidTestCase;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public final class ListViewsTest extends AndroidTestCase {

  public void testGetItemsWithPredicateReturnsFilteredItems() {
    ListView list = newListView(0, 1, 2, 3, 4, 5);
    assertEquals(singletonList(2), getItems(list, equalTo(2)));
  }

  public void testGetItemsWithClassReturnsItemsOfClass() {
    ListView list = newListView(1, "2", 3, "4");
    assertEquals(asList(1, 3), getItems(list, Integer.class));
  }

  public void testGetItemsReturnsAllItems() {
    ListView list = newListView(1, "2", true);
    assertEquals(asList(1, "2", true), getItems(list));
  }

  public void testGetCheckedItemsReturnsAllCheckedItemsOfGivenType() {
    ListView list = newListView("0", 1, "2", "3", 4, 5);
    setChecked(list, true, 0, 1, 2, 3, 4);
    assertEquals(3, getCheckedItems(list, String.class).size());
  }

  public void testGetCheckedItemsReturnsAllCurrentlyCheckedItemsOnly() {
    ListView list = newListView("0", "1");
    setChecked(list, true, 0, 1);
    setChecked(list, false, 1);
    assertEquals(1, getCheckedItems(list, Object.class).size());
  }

  private ListView newListView(Object... items) {
    ListView list = new ListView(getContext());
    list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    list.setAdapter(new ArrayAdapter<Object>(getContext(), 0, items));
    return list;
  }

  private void setChecked(ListView list, boolean checked, int... indices) {
    for (int i : indices) {
      list.setItemChecked(i, checked);
    }
  }
}
