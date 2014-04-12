package l.files.fse.integration;

import l.files.fse.WatchService;
import l.files.fse.WatchService_MOVE_TO_InitiatedTest;

public final class WatchService_Shared_MOVE_TO_InitiatedTest
    extends WatchService_MOVE_TO_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
