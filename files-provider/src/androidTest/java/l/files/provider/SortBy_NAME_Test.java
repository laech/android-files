package l.files.provider;

public final class SortBy_NAME_Test extends SortByBaseTest {

  public void testCompareIgnoresCase() {
    testSortMatches(SortBy.NAME,
        tmp().createFile("1"),
        tmp().createDir("2"),
        tmp().createFile("3"));
  }
}
