package l.files.fse.integration;

import l.files.fse.WatchService;
import l.files.fse.WatchService_LinkTest;

public final class WatchService_Shared_LinkTest
    extends WatchService_LinkTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
