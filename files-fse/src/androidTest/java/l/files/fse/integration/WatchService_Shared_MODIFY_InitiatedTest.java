package l.files.fse.integration;

import l.files.fse.WatchService;
import l.files.fse.WatchService_MODIFY_InitiatedTest;

public final class WatchService_Shared_MODIFY_InitiatedTest
    extends WatchService_MODIFY_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
