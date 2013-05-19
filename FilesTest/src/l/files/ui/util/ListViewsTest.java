package l.files.ui.util;

import android.widget.ListView;
import junit.framework.TestCase;

import static l.files.ui.util.ListViews.getCheckedItems;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class ListViewsTest extends TestCase {

  public void testGetCheckedItemsReturnsAllCheckedItems() {
    ListView listView = mock(ListView.class);
    given(listView.getCheckedItemCount()).willReturn(1);
    given(listView.getCount()).willReturn(5);
    given(listView.isItemChecked(2)).willReturn(true);
    assertEquals(1, getCheckedItems(listView).size());
  }
}
