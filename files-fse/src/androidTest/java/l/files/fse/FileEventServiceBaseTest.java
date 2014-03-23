package l.files.fse;

import l.files.common.testing.FileBaseTest;
import l.files.common.testing.TempDir;

abstract class FileEventServiceBaseTest extends FileBaseTest {

  private FileEventService manager;
  private EventServiceTester tester;
  private TempDir helper;

  @Override protected void setUp() throws Exception {
    super.setUp();
    manager = FileEventService.create();
    tester = EventServiceTester.create(manager, tmp());
    helper = TempDir.create("helper");
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    // TODO integration test sharing one manager for all tests
    manager.stopAll();
    helper.delete();
  }

  protected final FileEventService manager() {
    return manager;
  }

  protected final TempDir helper() {
    return helper;
  }

  protected final EventServiceTester tester() {
    return tester;
  }
}
