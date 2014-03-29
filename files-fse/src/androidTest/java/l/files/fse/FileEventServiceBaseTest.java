package l.files.fse;

import l.files.common.logging.Logger;
import l.files.common.testing.FileBaseTest;
import l.files.common.testing.TempDir;

abstract class FileEventServiceBaseTest extends FileBaseTest {

  private FileEventService service;
  private EventServiceTester tester;
  private TempDir helper;

  @Override protected void setUp() throws Exception {
    super.setUp();
    service = createService();
    tester = EventServiceTester.create(service, tmp());
    helper = TempDir.create("helper");
  }

  @Override protected void tearDown() throws Exception {
    if (stopServiceOnTearDown()) {
      service.stopAll();
    }
    helper.delete();
    Logger.resetDebugTagPrefix();
    super.tearDown();
  }

  protected FileEventService createService() {
    return new FileEventServiceImpl();
  }

  protected boolean stopServiceOnTearDown() {
    return true;
  }

  protected final FileEventService manager() {
    return service;
  }

  protected final TempDir helper() {
    return helper;
  }

  protected final EventServiceTester tester() {
    return tester;
  }
}
