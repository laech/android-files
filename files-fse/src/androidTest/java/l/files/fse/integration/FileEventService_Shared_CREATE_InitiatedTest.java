package l.files.fse.integration;

import l.files.fse.FileEventService;
import l.files.fse.FileEventService_CREATE_InitiatedTest;

public final class FileEventService_Shared_CREATE_InitiatedTest
    extends FileEventService_CREATE_InitiatedTest {

  @Override protected FileEventService createService() {
    return FileEventService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
