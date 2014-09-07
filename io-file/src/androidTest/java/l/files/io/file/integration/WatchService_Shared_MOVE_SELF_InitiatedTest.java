package l.files.io.file.integration;

import l.files.io.file.WatchService;
import l.files.io.file.WatchService_MOVE_SELF_InitiatedTest;

public final class WatchService_Shared_MOVE_SELF_InitiatedTest
    extends WatchService_MOVE_SELF_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
