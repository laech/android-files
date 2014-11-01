package l.files.fs.local;

public final class WatchService_Shared_ATTRIB_InitiatedTest
    extends WatchService_ATTRIB_InitiatedTest {

  @Override protected LocalWatchService createService() {
    return LocalWatchService.get();
  }

  @Override protected boolean stopServiceOnTearDown() {
    return false;
  }
}
