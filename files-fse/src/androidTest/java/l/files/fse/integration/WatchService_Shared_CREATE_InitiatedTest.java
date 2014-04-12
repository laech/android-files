package l.files.fse.integration;

import l.files.fse.WatchService;
import l.files.fse.WatchService_CREATE_InitiatedTest;

public final class WatchService_Shared_CREATE_InitiatedTest
    extends WatchService_CREATE_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
