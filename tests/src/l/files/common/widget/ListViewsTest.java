package l.files.common.widget;

import android.content.Context;
import android.test.AndroidTestCase;
import android.widget.ListView;

import static l.files.common.widget.ListViews.getCheckedItems;

public final class ListViewsTest extends AndroidTestCase {

  public void testGetCheckedItemsReturnsAllCheckedItems() {
    class MockListView extends ListView {
      public MockListView(Context context) {
        super(context);
      }

      @Override public int getCheckedItemCount() {
        return 1;
      }

      @Override public int getCount() {
        return 5;
      }

      @Override public boolean isItemChecked(int position) {
        return position == 2;
      }
    }

    ListView listView = new MockListView(getContext());
    assertEquals(1, getCheckedItems(listView).size());
  }
}
