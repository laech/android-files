package l.files.io.file.event.integration;

import l.files.io.file.event.WatchService;
import l.files.io.file.event.WatchService_LinkTest;

public final class WatchService_Shared_LinkTest
    extends WatchService_LinkTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
