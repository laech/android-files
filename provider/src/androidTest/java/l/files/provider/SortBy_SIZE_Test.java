package l.files.provider;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.write;

public final class SortBy_SIZE_Test extends SortByBaseTest {

  public void testSortsFilesBySize() throws Exception {
    File smaller = createFile("a", "short content");
    File larger = createFile("b", "longer content...........");
    testSortMatches(SortBy.SIZE, larger, smaller);
  }

  public void testSortsFilesByNameIfSizesEqual() throws Exception {
    File a = createFile("a", "content a");
    File b = createFile("b", "content b");
    testSortMatches(SortBy.SIZE, a, b);
  }

  public void testSortsDirLast() throws Exception {
    File f1 = tmp().createFile("a");
    File d1 = tmp().createDir("b");
    File f2 = tmp().createFile("c");
    testSortMatches(SortBy.SIZE, f1, f2, d1);
  }

  public void testSortsDirByName() throws Exception {
    File b = tmp().createDir("b");
    File a = tmp().createDir("a");
    testSortMatches(SortBy.SIZE, a, b);
  }

  private File createFile(String name, String content) {
    File file = tmp().createFile(name);
    try {
      write(file, content);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    return file;
  }
}
