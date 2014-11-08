package l.files.fs.local;

import com.google.common.collect.ImmutableSet;

import l.files.common.testing.FileBaseTest;
import l.files.fs.Path;

import static java.lang.Thread.sleep;
import static l.files.fs.WatchEvent.Listener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class WatchServiceIsWatchableTest extends FileBaseTest {

  public void testIsWatchable() throws Exception {
    Path dir1 = LocalPath.of(tmp().createDir("1"));
    Path dir2 = LocalPath.of(tmp().createDir("2"));
    Path root = LocalPath.of(tmp().get());
    try (LocalWatchService srv = new LocalWatchService(ImmutableSet.of(root))) {

      Listener listener = mock(Listener.class);
      srv.register(root, listener);

      assertFalse(srv.isWatchable(root));
      assertFalse(srv.isWatchable(dir1));
      assertFalse(srv.isWatchable(dir2));
      assertFalse(srv.isWatchable(root.resolve("a")));
      assertFalse(srv.isWatchable(root.resolve("a/b")));

      assertFalse(srv.isMonitored(root));
      assertFalse(srv.isMonitored(dir1));
      assertFalse(srv.isMonitored(dir2));

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
