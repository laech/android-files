package l.files.io.file.integration;

import l.files.io.file.WatchService;
import l.files.io.file.WatchService_CREATE_InitiatedTest;

public final class WatchService_Shared_CREATE_InitiatedTest
    extends WatchService_CREATE_InitiatedTest {

  @Override protected WatchService createService() {
    return WatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
