package l.files.io.file.event.integration;

import l.files.io.file.event.WatchService;
import l.files.io.file.event.WatchService_DELETE_InitiatedTest;

public final class WatchService_Shared_DELETE_InitiatedTest
    extends WatchService_DELETE_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
