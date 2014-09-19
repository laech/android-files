package l.files.io.file;

import com.google.common.collect.ImmutableSet;

import l.files.common.testing.FileBaseTest;

import static com.google.common.truth.Truth.ASSERT;
import static java.lang.Thread.sleep;
import static l.files.io.file.WatchEvent.Listener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class WatchServiceIgnoreTest extends FileBaseTest {

  public void testIgnore() throws Exception {
    Path dir1 = Path.from(tmp().createDir("1"));
    Path dir2 = Path.from(tmp().createDir("2"));
    Path root = Path.from(tmp().get());
    try (WatchServiceImpl srv = new WatchServiceImpl(ImmutableSet.of(root))) {

      Listener listener = mock(Listener.class);
      srv.register(root, listener);

      ASSERT.that(srv.isIgnored(root));
      ASSERT.that(srv.isIgnored(dir1));
      ASSERT.that(srv.isIgnored(dir2));
      ASSERT.that(srv.isIgnored(root.child("a")));
      ASSERT.that(srv.isIgnored(root.child("a/b")));

      ASSERT.that(srv.isMonitored(root)).isFalse();
      ASSERT.that(srv.isMonitored(dir1)).isFalse();
      ASSERT.that(srv.isMonitored(dir2)).isFalse();

      ASSERT.that(srv.hasObserver(root)).isFalse();
      ASSERT.that(srv.hasObserver(dir1)).isFalse();
      ASSERT.that(srv.hasObserver(dir2)).isFalse();

      tmp().createFile("a");
      tmp().createDir("b");
      sleep(1000);
      verifyZeroInteractions(listener);
    }
  }
}
