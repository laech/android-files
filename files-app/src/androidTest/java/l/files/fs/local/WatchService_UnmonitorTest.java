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
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertFalse(service().isMonitored(dir));
    assertFalse(service().hasObserver(dir));
  }

  public void testUnmonitorReMonitorIsOkay() {
    unlisten(tmpDir(), listen(tmpDir()));
    await(event(CREATE, "a"), newCreate("a", FileType.DIR));
  }

  public void testUnmonitorRemovesImmediateChildObserver() {
    Path dir = LocalPath.of(tmp().createDir("a"));

    WatchEvent.Listener listener = listen(tmpDir());
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertFalse(service().isMonitored(dir));
    assertFalse(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveImmediateChildObserverThatAreMonitored() {
    Path dir = LocalPath.of(tmp().createDir("a"));

    WatchEvent.Listener listener = listen(tmpDir());
    listen(new File(dir.toString()));
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveGrandChildObserver() {
    LocalPath dir = LocalPath.of(tmp().createDir("a/b"));

    WatchEvent.Listener listener = listen(tmpDir());
    listen(dir.parent().file());
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));
  }
}
