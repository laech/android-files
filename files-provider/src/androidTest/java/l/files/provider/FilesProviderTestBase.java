package l.files.provider;

import android.database.Cursor;

import l.files.common.logging.Logger;
import l.files.common.testing.FileBaseTest;
import l.files.common.testing.TempDir;

abstract class FilesProviderTestBase extends FileBaseTest {

  private TempDir helper;
  private FilesProviderTester tester;

  @Override protected void setUp() throws Exception {
    super.setUp();
    helper = TempDir.create("helper_");
    tester = createTester(tmp());
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    Logger.resetDebugTagPrefix();
    helper.delete();
  }

  /**
   * Returns a second temp directory for testing purpose. This is
   * created/destroyed per test.
   */
  protected TempDir helper() {
    return helper;
  }

  /**
   * Returns a test helper created with {@link #tmp()}. This is
   * created/destroyed per test.
   */
  protected FilesProviderTester tester() {
    return tester;
  }

  /**
   * Creates a new instance of {@link FilesProviderTester} using the context of
   * this test case and the given {@code dir}.
   */
  protected FilesProviderTester createTester(TempDir dir) {
    return FilesProviderTester.create(getContext(), dir);
  }

  /**
   * Wait for a short while and make a query to the content provider for {@link
   * #tmp()}, assert the result is empty.
   */
  protected void awaitEmpty() {
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }

    Cursor cursor = tester().query();
    //noinspection TryFinallyCanBeTryWithResources
    try {
      assertEquals(0, cursor.getCount());
    } finally {
      cursor.close();
    }
  }
}
