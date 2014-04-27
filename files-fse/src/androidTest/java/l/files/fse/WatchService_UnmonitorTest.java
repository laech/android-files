package l.files.fse;

import l.files.io.Path;

import static l.files.fse.WatchEvent.Kind.CREATE;
import static l.files.fse.WatchServiceBaseTest.FileType.DIR;
import static org.mockito.Mockito.mock;

public final class WatchService_UnmonitorTest extends WatchServiceBaseTest {

  public void testUnmonitorRootDirChildren() {
    WatchEvent.Listener listener = mock(WatchEvent.Listener.class);
    service().register(Path.from("/"), listener);
    assertTrue(service().toString(), service().hasObserver(Path.from("/dev")));
    assertTrue(service().toString(), service().hasObserver(Path.from("/data")));

    service().unregister(Path.from("/"), listener);
    assertFalse(service().toString(), service().hasObserver(Path.from("/dev")));
    assertFalse(service().toString(), service().hasObserver(Path.from("/data")));
  }

  public void testUnmonitorSelf() {
    Path dir = Path.from(tmp().get());

    WatchEvent.Listener listener = listen(tmpDir());
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertFalse(service().isMonitored(dir));
    assertFalse(service().hasObserver(dir));
  }

  public void testUnmonitorReMonitorIsOkay() {
    unlisten(tmpDir(), listen(tmpDir()));
    await(event(CREATE, "a"), newCreate("a", DIR));
  }

  public void testUnmonitorRemovesImmediateChildObserver() {
    Path dir = Path.from(tmp().createDir("a"));

    WatchEvent.Listener listener = listen(tmpDir());
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertFalse(service().isMonitored(dir));
    assertFalse(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveImmediateChildObserverThatAreMonitored() {
    Path dir = Path.from(tmp().createDir("a"));

    WatchEvent.Listener listener = listen(tmpDir());
    listen(dir.toFile());
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveGrandChildObserver() {
    Path dir = Path.from(tmp().createDir("a/b"));

    WatchEvent.Listener listener = listen(tmpDir());
    listen(dir.parent().toFile());
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    unlisten(tmpDir(), listener);
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));
  }
}
