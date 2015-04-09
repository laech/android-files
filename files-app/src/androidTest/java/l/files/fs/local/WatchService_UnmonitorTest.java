package l.files.fs.local;

import java.io.File;
import java.io.IOException;

import l.files.fs.Resource;
import l.files.fs.WatchEvent;

import static l.files.fs.WatchEvent.Kind.CREATE;
import static org.mockito.Mockito.mock;

public final class WatchService_UnmonitorTest extends WatchServiceBaseTest {

    public void testUnmonitorRootDirChildren() throws IOException {
        WatchEvent.Listener listener = mock(WatchEvent.Listener.class);
        service().register(LocalResource.create(new File("/")), listener);
        assertTrue(service().toString(), service().hasObserver(LocalResource.create(new File("/mnt"))));
        assertTrue(service().toString(), service().hasObserver(LocalResource.create(new File("/data"))));

        service().unregister(LocalResource.create(new File("/")), listener);
        assertFalse(service().toString(), service().hasObserver(LocalResource.create(new File("/mnt"))));
        assertFalse(service().toString(), service().hasObserver(LocalResource.create(new File("/data"))));
    }

    public void testUnmonitorSelf() {
        Resource dir = LocalResource.create(tmp().get());

        WatchEvent.Listener listener = listen(tmpDir());
        assertTrue(service().isRegistered(dir));
        assertTrue(service().hasObserver(dir));

        unlisten(tmpDir(), listener);
        assertFalse(service().isRegistered(dir));
        assertFalse(service().hasObserver(dir));
    }

    public void testUnmonitorReMonitorIsOkay() {
        unlisten(tmpDir(), listen(tmpDir()));
        await(event(CREATE, "a"), newCreate("a", FileType.DIR));
    }

    public void testUnmonitorRemovesImmediateChildObserver() {
        Resource dir = LocalResource.create(tmp().createDir("a"));

        WatchEvent.Listener listener = listen(tmpDir());
        assertFalse(service().isRegistered(dir));
        assertTrue(service().hasObserver(dir));

        unlisten(tmpDir(), listener);
        assertFalse(service().isRegistered(dir));
        assertFalse(service().hasObserver(dir));
    }

    public void testUnmonitorDoesNotRemoveImmediateChildObserverThatAreMonitored() {
        LocalResource dir = LocalResource.create(tmp().createDir("a"));

        WatchEvent.Listener listener = listen(tmpDir());
        listen(dir.getFile());
        assertTrue(service().isRegistered(dir));
        assertTrue(service().hasObserver(dir));

        unlisten(tmpDir(), listener);
        assertTrue(service().isRegistered(dir));
        assertTrue(service().hasObserver(dir));
    }

    public void testUnmonitorDoesNotRemoveGrandChildObserver() {
        LocalResource dir = LocalResource.create(tmp().createDir("a/b"));

        WatchEvent.Listener listener = listen(tmpDir());
        listen(dir.getFile().getParentFile());
        assertFalse(service().isRegistered(dir));
        assertTrue(service().hasObserver(dir));

        unlisten(tmpDir(), listener);
        assertFalse(service().isRegistered(dir));
        assertTrue(service().hasObserver(dir));
    }

}
