package l.files.io.file.integration;

import l.files.io.file.WatchService;
import l.files.io.file.WatchService_ATTRIB_InitiatedTest;

public final class WatchService_Shared_ATTRIB_InitiatedTest
    extends WatchService_ATTRIB_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
