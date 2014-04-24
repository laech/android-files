package l.files.fse;

import l.files.io.Path;

public final class WatchService_MonitorTest extends WatchServiceBaseTest {

  public void testMonitorRootDirChildren() {
    service().monitor(Path.from("/"));
    assertTrue(service().toString(), service().hasObserver(Path.from("/dev")));
    assertTrue(service().toString(), service().hasObserver(Path.from("/data")));
  }
}
