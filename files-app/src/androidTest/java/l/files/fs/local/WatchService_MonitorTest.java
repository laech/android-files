package l.files.fs.local;

import java.io.IOException;

import l.files.fs.WatchEvent;

import static org.mockito.Mockito.mock;

public final class WatchService_MonitorTest extends WatchServiceBaseTest {

  public void testMonitorRootDirChildren() throws IOException {
    service().register(LocalPath.from("/"), mock(WatchEvent.Listener.class));
    assertTrue(service().toString(), service().hasObserver(LocalPath.from("/mnt")));
    assertTrue(service().toString(), service().hasObserver(LocalPath.from("/data")));
  }
}
