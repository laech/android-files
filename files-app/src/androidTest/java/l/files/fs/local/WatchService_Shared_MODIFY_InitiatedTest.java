package l.files.fs.local;

public final class WatchService_Shared_MODIFY_InitiatedTest
    extends WatchService_MODIFY_InitiatedTest {

  @Override protected LocalWatchService createService() {
    return LocalWatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
