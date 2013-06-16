package l.files.ui.app.files;

import junit.framework.TestCase;

import java.io.File;

import static java.util.Arrays.asList;

public final class SortByNameTest extends TestCase {

  private SortByName sort;

  @Override protected void setUp() throws Exception {
    super.setUp();
    sort = new SortByName();
  }

  public void testSortByNameComparesIgnoreCase() {
    File x = new File("/x");
    File y = new File("/Y");
    assertEquals(asList(x, y), sort.apply(x, y));
  }

  public void testSortByNameComparesNamePartOnly() {
    File x = new File("/1/a");
    File y = new File("/0/a");
    assertEquals(asList(x, y), sort.apply(x, y));
  }
}
