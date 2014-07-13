package l.files.provider;

import java.io.File;

public final class SortBy_DATE_Test extends SortByBaseTest {

  public void testSortByDateDesc() throws Exception {
    testSortMatches(SortBy.DATE,
        createDirLastModified("b", 3000),
        createFileLastModified("a", 2000),
        createDirLastModified("c", 1000));
  }

  public void testSortByNameIfDatesEqual() throws Exception {
    testSortMatches(SortBy.DATE,
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
