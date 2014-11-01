package l.files.fs.local;

public final class WatchService_Shared_DELETE_InitiatedTest
    extends WatchService_DELETE_InitiatedTest {

  @Override protected LocalWatchService createService() {
    return LocalWatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
