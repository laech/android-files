package l.files.fs.local;

import com.google.common.collect.ImmutableSet;

import l.files.common.testing.FileBaseTest;
import l.files.fs.Resource;

import static java.lang.Thread.sleep;
import static l.files.fs.WatchEvent.Listener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class WatchServiceIsWatchableTest extends FileBaseTest {

    public void testIsWatchable() throws Exception {
        Resource dir1 = LocalResource.create(tmp().createDir("1"));
        Resource dir2 = LocalResource.create(tmp().createDir("2"));
        Resource root = LocalResource.create(tmp().get());
        try (LocalWatchService srv = new LocalWatchService(ImmutableSet.of(root.getPath()))) {

            Listener listener = mock(Listener.class);
            srv.register(root, listener);

            assertFalse(srv.isWatchable(root));
            assertFalse(srv.isWatchable(dir1));
            assertFalse(srv.isWatchable(dir2));
            assertFalse(srv.isWatchable(root.resolve("a")));
            assertFalse(srv.isWatchable(root.resolve("a/b")));

            assertFalse(srv.isRegistered(root));
            assertFalse(srv.isRegistered(dir1));
            assertFalse(srv.isRegistered(dir2));

            assertFalse(srv.hasObserver(root));
            assertFalse(srv.hasObserver(dir1));
            assertFalse(srv.hasObserver(dir2));

            tmp().createFile("a");
            tmp().createDir("b");
            sleep(1000);
            verifyZeroInteractions(listener);
        }
    }

}
