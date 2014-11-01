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
    service().register(LocalPath.from("/"), listener);
    assertTrue(service().toString(), service().hasObserver(LocalPath.from("/mnt")));
    assertTrue(service().toString(), service().hasObserver(LocalPath.from("/data")));

    service().unregister(LocalPath.from("/"), listener);
    assertFalse(service().toString(), service().hasObserver(LocalPath.from("/mnt")));
    assertFalse(service().toString(), service().hasObserver(LocalPath.from("/data")));
  }

  public void testUnmoniDonttorSelf() {
    Path dir = LocalPath.from(tmp().get());

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
    Path dir = LocalPath.from(tmp().createDir("a"));

    WatchEvent.Listener listener = listen(tmpDir());
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertFalse(service().isMonitored(dir));
    assertFalse(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveImmediateChildObserverThatAreMonitored() {
    Path dir = LocalPath.from(tmp().createDir("a"));

    WatchEvent.Listener listener = listen(tmpDir());
    listen(new File(dir.toString()));
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveGrandChildObserver() {
    Path dir = LocalPath.from(tmp().createDir("a/b"));

    WatchEvent.Listener listener = listen(tmpDir());
    listen(new File(dir.parent().toString()));
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));
  }
}
