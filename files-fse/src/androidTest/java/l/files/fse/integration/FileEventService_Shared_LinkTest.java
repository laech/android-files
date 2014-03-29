package l.files.fse.integration;

import l.files.fse.FileEventService;
import l.files.fse.FileEventService_LinkTest;

public final class FileEventService_Shared_LinkTest
    extends FileEventService_LinkTest {

  @Override protected FileEventService createService() {
    return FileEventService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
