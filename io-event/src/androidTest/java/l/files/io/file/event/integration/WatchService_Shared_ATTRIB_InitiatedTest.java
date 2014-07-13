package l.files.io.file.event.integration;

import l.files.io.file.event.WatchService;
import l.files.io.file.event.WatchService_ATTRIB_InitiatedTest;

public final class WatchService_Shared_ATTRIB_InitiatedTest
    extends WatchService_ATTRIB_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
