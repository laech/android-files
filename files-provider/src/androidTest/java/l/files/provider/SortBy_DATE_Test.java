package l.files.provider;

import java.io.File;

public final class SortBy_DATE_Test extends SortByBaseTest {

  public void testSortByDateDesc() {
    testSortMatches(SortBy.DATE,
        createDirLastModified("b", 3),
        createFileLastModified("a", 2),
        createDirLastModified("c", 1));
  }

  public void testSortByNameIfDatesEqual() {
    testSortMatches(SortBy.DATE,
        createFileLastModified("a", 1),
        createDirLastModified("b", 1),
        createFileLastModified("c", 1));
  }

  private File createFileLastModified(String name, long modified) {
    return mockLastModified(tmp().createFile(name), modified);
  }

  private File createDirLastModified(String name, long modified) {
    return mockLastModified(tmp().createDir(name), modified);
  }

  private File mockLastModified(File file, final long modified) {
    return new File(file.getPath()) {
      @Override public long lastModified() {
        return modified;
      }
    };
  }
}
