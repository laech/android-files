package l.files.fse.integration;

import l.files.fse.FileEventService;
import l.files.fse.FileEventService_ATTRIB_InitiatedTest;

public final class FileEventService_Shared_ATTRIB_InitiatedTest
    extends FileEventService_ATTRIB_InitiatedTest {

  @Override protected FileEventService createService() {
    return FileEventService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
