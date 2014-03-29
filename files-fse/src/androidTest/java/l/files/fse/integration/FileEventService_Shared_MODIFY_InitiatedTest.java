package l.files.fse.integration;

import l.files.fse.FileEventService;
import l.files.fse.FileEventService_MODIFY_InitiatedTest;

public final class FileEventService_Shared_MODIFY_InitiatedTest
    extends FileEventService_MODIFY_InitiatedTest {

  @Override protected FileEventService createService() {
    return FileEventService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
