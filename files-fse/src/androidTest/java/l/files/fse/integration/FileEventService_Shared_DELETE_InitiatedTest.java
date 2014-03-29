package l.files.fse.integration;

import l.files.fse.FileEventService;
import l.files.fse.FileEventService_DELETE_InitiatedTest;

public final class FileEventService_Shared_DELETE_InitiatedTest
    extends FileEventService_DELETE_InitiatedTest {

  @Override protected FileEventService createService() {
    return FileEventService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
