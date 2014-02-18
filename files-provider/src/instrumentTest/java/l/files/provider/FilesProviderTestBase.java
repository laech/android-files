package l.files.provider;

import android.test.AndroidTestCase;

import l.files.common.testing.TempDir;

abstract class FilesProviderTestBase extends AndroidTestCase {

  private TempDir tmp;
  private TempDir helper;
  private QueryTester tester;

  @Override protected void setUp() throws Exception {
    super.setUp();
    tmp = TempDir.create();
    helper = TempDir.create();
    tester = QueryTester.create(getContext(), tmp);
  }

  @Override protected void tearDown() throws Exception {
    tmp.delete();
    helper.delete();
    super.tearDown();
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
   * Returns a test helper. This is created/destroyed per test.
   */
  protected QueryTester tester() {
    return tester;
  }
}
