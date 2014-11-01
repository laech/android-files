package l.files.fs.local;

public final class WatchService_Shared_MOVE_TO_InitiatedTest
    extends WatchService_MOVE_TO_InitiatedTest {

  @Override protected LocalWatchService createService() {
    return LocalWatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
