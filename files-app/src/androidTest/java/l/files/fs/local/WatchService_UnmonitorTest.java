package l.files.fs.local;

import java.io.File;
import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.WatchEvent;

import static l.files.fs.WatchEvent.Kind.CREATE;
import static org.mockito.Mockito.mock;

public final class WatchService_UnmonitorTest extends WatchServiceBaseTest {

  public void testUnmonitorRootDirChildren() throws IOException {
    WatchEvent.Listener listener = mock(WatchEvent.Listener.class);
    service().register(LocalPath.of("/"), listener);
    assertTrue(service().toString(), service().hasObserver(LocalPath.of("/mnt")));
    assertTrue(service().toString(), service().hasObserver(LocalPath.of("/data")));

    service().unregister(LocalPath.of("/"), listener);
    assertFalse(service().toString(), service().hasObserver(LocalPath.of("/mnt")));
    assertFalse(service().toString(), service().hasObserver(LocalPath.of("/data")));
  }

  public void testUnmoniDonttorSelf() {
    Path dir = LocalPath.of(tmp().get());

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
    Path dir = LocalPath.of(tmp().createDir("a"));

    WatchEvent.Listener listener = listen(tmpDir());
    assertFalse(service().isRegistered(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertFalse(service().isRegistered(dir));
    assertFalse(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveImmediateChildObserverThatAreMonitored() {
    Path dir = LocalPath.of(tmp().createDir("a"));

    WatchEvent.Listener listener = listen(tmpDir());
    listen(new File(dir.toString()));
    assertTrue(service().isRegistered(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertTrue(service().isRegistered(dir));
    assertTrue(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveGrandChildObserver() {
    LocalPath dir = LocalPath.of(tmp().createDir("a/b"));

    WatchEvent.Listener listener = listen(tmpDir());
    listen(dir.getParent().getFile());
    assertFalse(service().isRegistered(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertFalse(service().isRegistered(dir));
    assertTrue(service().hasObserver(dir));
  }
}
