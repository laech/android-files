package l.files.provider;

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
    tester().verifyQuery(false, "a", "d");
  }

  public void testQueryMultipleDirs() {
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
