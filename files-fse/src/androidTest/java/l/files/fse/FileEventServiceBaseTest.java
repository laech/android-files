package l.files.fse;

import l.files.common.testing.FileBaseTest;
import l.files.common.testing.TempDir;

abstract class FileEventServiceBaseTest extends FileBaseTest {

  private FileEventService service;
  private FileEventServiceTester tester;
  private TempDir helper;

  @Override protected void setUp() throws Exception {
    super.setUp();
    service = createService();
    tester = FileEventServiceTester.create(service, tmp());
    helper = TempDir.create("helper");
  }

  @Override protected void tearDown() throws Exception {
    if (stopServiceOnTearDown()) {
      service.stopAll();
    }
    helper.delete();
    super.tearDown();
  }

  protected FileEventService createService() {
    return new FileEventServiceImpl();
  }

  protected boolean stopServiceOnTearDown() {
    return true;
  }

  protected final FileEventService service() {
    return service;
  }

  protected final TempDir helper() {
    return helper;
  }

  protected final FileEventServiceTester tester() {
    return tester;
  }
}