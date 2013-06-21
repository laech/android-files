package l.files.settings;

import android.test.AndroidTestCase;

import java.io.File;

import static java.util.Arrays.asList;

public final class SortByNameTest extends AndroidTestCase {

  private SortByName sort;

  @Override protected void setUp() throws Exception {
    super.setUp();
    sort = new SortByName();
  }

  public void testSortByNameComparesIgnoreCase() {
    File x = new File("/x");
    File y = new File("/Y");
    assertEquals(asList(x, y), sort.transform(getContext(), x, y));
  }

  public void testSortByNameComparesNamePartOnly() {
    File x = new File("/1/a");
    File y = new File("/0/a");
    assertEquals(asList(x, y), sort.transform(getContext(), x, y));
  }
}
