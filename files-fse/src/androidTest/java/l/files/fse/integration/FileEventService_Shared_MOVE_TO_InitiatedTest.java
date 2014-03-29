package l.files.fse.integration;

import l.files.fse.FileEventService;
import l.files.fse.FileEventService_MOVE_TO_InitiatedTest;

public final class FileEventService_Shared_MOVE_TO_InitiatedTest
    extends FileEventService_MOVE_TO_InitiatedTest {

  @Override protected FileEventService createService() {
    return FileEventService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
