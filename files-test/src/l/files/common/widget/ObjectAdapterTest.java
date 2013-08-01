package l.files.common.widget;

import junit.framework.TestCase;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;

public final class ObjectAdapterTest extends TestCase {

  private ObjectAdapter adapter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    adapter = new ObjectAdapter();
  }

  public void testAdd() {
    adapter.add("a");
    adapter.add("b");
    assertThat(adapter.getCount()).isEqualTo(2);
  }

  public void testAddAll() {
    adapter.addAll(asList("a", "a"));
    assertThat(adapter.getCount()).isEqualTo(2);
  }

  public void testClear() {
    adapter.add("a");
    adapter.add("b");
    adapter.clear();
    assertThat(adapter.getCount()).isEqualTo(0);
  }
}
