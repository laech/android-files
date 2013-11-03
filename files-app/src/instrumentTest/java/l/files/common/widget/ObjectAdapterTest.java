package l.files.common.widget;

import junit.framework.TestCase;

import static java.util.Arrays.asList;

public final class ObjectAdapterTest extends TestCase {

  private ObjectAdapter adapter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    adapter = new ObjectAdapter();
  }

  public void testAdd() {
    adapter.add("a");
    adapter.add("b");
    assertEquals(2, adapter.getCount());
  }

  public void testAddAll() {
    adapter.addAll(asList("a", "a"));
    assertEquals(2, adapter.getCount());
  }

  public void testClear() {
    adapter.add("a");
    adapter.add("b");
    adapter.clear();
    assertEquals(0, adapter.getCount());
  }
}
