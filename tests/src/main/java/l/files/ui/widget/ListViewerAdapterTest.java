package l.files.ui.widget;

import junit.framework.TestCase;

import static org.fest.assertions.api.Assertions.assertThat;

public final class ListViewerAdapterTest extends TestCase {

  private ListViewerAdapter adapter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    adapter = new ListViewerAdapter();
  }

  public void testGetCount_returnsSizeOfSize() {
    adapter.items.add("a");
    adapter.items.add("a");
    assertThat(adapter.getCount()).isEqualTo(2);
  }

  public void testGetItem_returnsItemInList() {
    adapter.items.add("a");
    assertThat(adapter.getItem(0)).isEqualTo("a");
  }

  public void testGetItemId_returnsPosition() {
    adapter.items.add("a");
    adapter.items.add("b");
    assertThat(adapter.getItemId(0)).isEqualTo(0);
    assertThat(adapter.getItemId(1)).isEqualTo(1);
  }

}
