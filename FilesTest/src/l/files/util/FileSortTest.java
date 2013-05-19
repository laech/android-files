package l.files.util;

import junit.framework.TestCase;

import java.io.File;

import static l.files.util.FileSort.BY_NAME;

public final class FileSortTest extends TestCase {

  public void testSortByNameComparesIgnoreCase() {
    File x = new File("/x");
    File y = new File("/Y");
    assertEquals(-1, BY_NAME.compare(x, y));
  }

  public void testSortByNameComparesNamePartOnly() {
    File x = new File("/1/a");
    File y = new File("/0/a");
    assertEquals(0, BY_NAME.compare(x, y));
  }
}
