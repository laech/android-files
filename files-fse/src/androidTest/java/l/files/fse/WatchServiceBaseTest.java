package l.files.fse;

import l.files.common.testing.FileBaseTest;
import l.files.common.testing.TempDir;

abstract class WatchServiceBaseTest extends FileBaseTest {

  private WatchService service;
  private WatchServiceTester tester;
  private TempDir helper;

  @Override protected void setUp() throws Exception {
    super.setUp();
    service = createService();
    tester = WatchServiceTester.create(service, tmp());
    helper = TempDir.create("helper");
  }

  @Override protected void tearDown() throws Exception {
    if (stopServiceOnTearDown()) {
      service.stopAll();
    }
    helper.delete();
    super.tearDown();
  }

  protected WatchService createService() {
    return new WatchServiceImpl();
  }

  protected boolean stopServiceOnTearDown() {
    return true;
  }

  protected final WatchService service() {
    return service;
  }

  protected final TempDir helper() {
    return helper;
  }

  protected final WatchServiceTester tester() {
    return tester;
  }
}
