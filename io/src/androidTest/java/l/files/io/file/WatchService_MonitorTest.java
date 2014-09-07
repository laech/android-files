package l.files.io.file;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public final class WatchService_MonitorTest extends WatchServiceBaseTest {

  public void testMonitorRootDirChildren() throws IOException {
    service().register(Path.from("/"), mock(WatchEvent.Listener.class));
    assertTrue(service().toString(), service().hasObserver(Path.from("/dev")));
    assertTrue(service().toString(), service().hasObserver(Path.from("/data")));
  }
}
