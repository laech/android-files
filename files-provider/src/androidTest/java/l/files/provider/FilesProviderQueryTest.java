package l.files.provider;

import static l.files.common.database.DataTypes.booleanToString;
import static l.files.provider.FilesContract.FileInfo.HIDDEN;

public final class FilesProviderQueryTest extends FilesProviderTestBase {

  public void testQueryFile() {
    tmp().createFile("a");
    tester().verify();
  }

  public void testQueryDir() {
    tmp().createDir("b");
    tester().verify();
  }

  public void testQueryMultiple() {
    tmp().createFile("a");
    tmp().createDir("b");
    tmp().createFile("c");
    tmp().createFile("d");
    tester().verify();
  }

  public void testQueryHiddenFiles() {
    tmp().createFile("a");
    tmp().createFile(".b");
    tmp().createDir(".c");
    tmp().createDir("d");
    String selection = HIDDEN + "=?";
    String[] selectionArgs = {booleanToString(false)};
    tester().verifyQuery(selection, selectionArgs, "a", "d");
  }
}
