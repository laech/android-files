package l.files.provider;

import l.files.common.logging.Logger;

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

  public void testQueryMultipleDirs() {
    Logger.setDebugTagPrefix("testQueryMultipleDirs");
    tester()
        .awaitCreateDir("a")
        .awaitCreateFile("b")
        .awaitCreateDir("c/a")
        .awaitCreateFile("c/b")
        .awaitCreateFile("c/c")
        .awaitCreateFile("d/a")
        .awaitCreateFile("d/b")
        .verify()
        .verify("c")
        .verify("d");
  }

  public void testQueryExistingContent() {
    Logger.setDebugTagPrefix("testQueryExistingContent");
    tmp().createFile("a");
    tmp().createFile("b");
    tmp().createFile("c/a");
    tmp().createFile("c/b");
    tmp().createFile("d/a");
    tmp().createFile("d/b");
    tmp().createFile("d/c");

    tester().verify();
    tester().verify("c");
    tester().verify("d");
  }
}
