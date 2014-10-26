package l.files.fs.local;

import l.files.fs.local.WatchService;
import l.files.fs.local.WatchService_MOVE_FROM_InitiatedTest;

public final class WatchService_Shared_MOVE_FROM_InitiatedTest
    extends WatchService_MOVE_FROM_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
