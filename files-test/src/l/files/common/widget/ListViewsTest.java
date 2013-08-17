package l.files.common.widget;

import static l.files.common.widget.ListViews.getCheckedItems;

import android.test.AndroidTestCase;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public final class ListViewsTest extends AndroidTestCase {

  public void testGetCheckedItemsReturnsAllCheckedItemsOfGivenType() {
    ListView list = new ListView(getContext());
    list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    list.setAdapter(new ArrayAdapter<Object>(getContext(), 0, new Object[]{
        "0", 1, "2", "3", 4, 5
    }));
    list.setItemChecked(0, true);
    list.setItemChecked(1, true);
    list.setItemChecked(2, true);
    list.setItemChecked(3, true);
    list.setItemChecked(4, true);

    assertEquals(3, getCheckedItems(list, String.class).size());
  }
}
