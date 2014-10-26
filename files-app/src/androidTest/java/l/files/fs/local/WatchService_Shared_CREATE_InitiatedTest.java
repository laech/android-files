package l.files.fs.local;

import l.files.fs.local.WatchService;
import l.files.fs.local.WatchService_CREATE_InitiatedTest;

public final class WatchService_Shared_CREATE_InitiatedTest
    extends WatchService_CREATE_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
