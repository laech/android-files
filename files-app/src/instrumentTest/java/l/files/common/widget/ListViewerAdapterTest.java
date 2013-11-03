package l.files.common.widget;

import junit.framework.TestCase;

public final class ListViewerAdapterTest extends TestCase {

  private ListViewerAdapter adapter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    adapter = new ListViewerAdapter();
  }

  public void testGetCount_returnsSizeOfSize() {
    adapter.items.add("a");
    adapter.items.add("a");
    assertEquals(2, adapter.getCount());
  }

  public void testGetItem_returnsItemInList() {
    adapter.items.add("a");
    assertEquals("a", adapter.getItem(0));
  }

  public void testGetItemId_returnsPosition() {
    adapter.items.add("a");
    adapter.items.add("b");
    assertEquals(0, adapter.getItemId(0));
    assertEquals(1, adapter.getItemId(1));
  }
}
