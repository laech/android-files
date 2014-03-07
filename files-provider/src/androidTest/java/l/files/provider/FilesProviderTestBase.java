package l.files.provider;

import android.database.Cursor;

import java.util.concurrent.Executor;

import l.files.common.logging.Logger;
import l.files.common.testing.BaseTest;
import l.files.common.testing.TempDir;

abstract class FilesProviderTestBase extends BaseTest {

  private TempDir tmp;
  private TempDir helper;
  private FilesProviderTester tester;
  private Executor originalExecutor;

  @Override protected void setUp() throws Exception {
    super.setUp();
    tmp = TempDir.create();
    helper = TempDir.create();
    tester = createTester(tmp);

    // TODO remove this to make it a more realistic test
    originalExecutor = FilesDb.executor;
    FilesDb.executor = new SameThreadExecutor();
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    FilesDb.executor = originalExecutor;
    Logger.resetDebugTagPrefix();
    tmp.delete();
    helper.delete();
  }

  /**
   * Returns a temp directory for testing. This is created/destroyed per test.
   */
  protected TempDir tmp() {
    return tmp;
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

  private static class SameThreadExecutor implements Executor {
    @Override
    public void execute(@SuppressWarnings("NullableProblems") Runnable cmd) {
      cmd.run();
    }
  }
}
