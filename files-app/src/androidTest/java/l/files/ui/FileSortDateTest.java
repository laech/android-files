package l.files.ui;

import java.io.File;
import java.util.Locale;

public final class FileSortDateTest extends FileSortTest {

  public void testSortByDateDesc() throws Exception {
    testSortMatches(FileSort.MODIFIED.newComparator(Locale.getDefault()),
        createDirLastModified("b", 3000),
        createFileLastModified("a", 2000),
        createDirLastModified("c", 1000));
  }

  public void testSortByNameIfDatesEqual() throws Exception {
    testSortMatches(FileSort.MODIFIED.newComparator(Locale.getDefault()),
        createFileLastModified("a", 1),
        createDirLastModified("b", 1),
        createFileLastModified("c", 1));
  }

  private File createFileLastModified(String name, long modified) {
    return setLastModified(tmp().createFile(name), modified);
  }

  private File createDirLastModified(String name, long modified) {
    return setLastModified(tmp().createDir(name), modified);
  }

  private File setLastModified(File file, final long modified) {
    assertTrue(file.setLastModified(modified));
    return file;
  }
}
