package l.files.fs.local;

public final class WatchService_Shared_CREATE_InitiatedTest
    extends WatchService_CREATE_InitiatedTest {

  @Override protected LocalWatchService createService() {
    return LocalWatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
