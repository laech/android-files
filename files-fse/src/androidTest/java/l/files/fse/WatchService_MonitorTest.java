package l.files.fse;

import l.files.io.file.Path;

import static org.mockito.Mockito.mock;

public final class WatchService_MonitorTest extends WatchServiceBaseTest {

  public void testMonitorRootDirChildren() {
    service().register(Path.from("/"), mock(WatchEvent.Listener.class));
    assertTrue(service().toString(), service().hasObserver(Path.from("/dev")));
    assertTrue(service().toString(), service().hasObserver(Path.from("/data")));
  }
}
