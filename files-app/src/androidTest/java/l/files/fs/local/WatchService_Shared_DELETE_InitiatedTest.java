package l.files.fs.local;

import l.files.fs.local.WatchService;
import l.files.fs.local.WatchService_DELETE_InitiatedTest;

public final class WatchService_Shared_DELETE_InitiatedTest
    extends WatchService_DELETE_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
