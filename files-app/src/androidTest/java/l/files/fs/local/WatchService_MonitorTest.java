package l.files.fs.local;

import java.io.IOException;

import l.files.fs.WatchEvent;

import static org.mockito.Mockito.mock;

public final class WatchService_MonitorTest extends WatchServiceBaseTest {

  public void testMonitorRootDirChildren() throws IOException {
    service().register(LocalPath.of("/"), mock(WatchEvent.Listener.class));
    assertTrue(service().toString(), service().hasObserver(LocalPath.of("/mnt")));
    assertTrue(service().toString(), service().hasObserver(LocalPath.of("/data")));
  }
}
