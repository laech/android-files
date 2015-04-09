package l.files.fs.local;

import java.io.File;
import java.io.IOException;

import l.files.fs.Resource;
import l.files.fs.WatchEvent;

import static org.mockito.Mockito.mock;

public final class WatchService_MonitorTest extends WatchServiceBaseTest {

    public void testMonitorRootDirChildren() throws IOException {
        Resource resource = LocalResource.create(new File("/"));
        assertFalse(service().isRegistered(resource));

        service().register(resource, mock(WatchEvent.Listener.class));

        assertTrue(service().isRegistered(resource));
        assertTrue(service().toString(), service().hasObserver(LocalResource.create(new File("/mnt"))));
        assertTrue(service().toString(), service().hasObserver(LocalResource.create(new File("/data"))));
    }

}
