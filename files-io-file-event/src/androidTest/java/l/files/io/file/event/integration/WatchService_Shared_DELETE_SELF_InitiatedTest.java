package l.files.io.file.event.integration;

import l.files.io.file.event.WatchService;
import l.files.io.file.event.WatchService_DELETE_SELF_InitiatedTest;

public final class WatchService_Shared_DELETE_SELF_InitiatedTest
    extends WatchService_DELETE_SELF_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}