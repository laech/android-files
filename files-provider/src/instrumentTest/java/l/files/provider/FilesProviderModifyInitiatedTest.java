package l.files.provider;

/**
 * Tests file system operations started with modifying files.
 *
 * @see android.os.FileObserver#MODIFY
 */
public final class FilesProviderModifyInitiatedTest
    extends FilesProviderTestBase {

  public void testModifyFile() {
    tmp().createFile("a");
    tester()
        .awaitModify("a")
        .verify();
  }
}
