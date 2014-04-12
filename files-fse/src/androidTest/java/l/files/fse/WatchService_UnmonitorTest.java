package l.files.fse;

import l.files.io.Path;

public final class WatchService_UnmonitorTest extends WatchServiceBaseTest {

  public void testUnmonitorRootDirChildren() {
    service().monitor(Path.from("/"));
    assertTrue(service().toString(), service().hasObserver(Path.from("/dev")));
    assertTrue(service().toString(), service().hasObserver(Path.from("/data")));

    service().unmonitor(Path.from("/"));
    assertFalse(service().toString(), service().hasObserver(Path.from("/dev")));
    assertFalse(service().toString(), service().hasObserver(Path.from("/data")));
  }

  public void testUnmonitorSelf() {
    Path dir = Path.from(tmp().get());

    tester().monitor();
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    tester().unmonitor();
    assertFalse(service().isMonitored(dir));
    assertFalse(service().hasObserver(dir));
  }

  public void testUnmonitorReMonitorIsOkay() {
    tester()
        .monitor()
        .unmonitor()
        .monitor()
        .awaitCreateDir("a");
  }

  public void testUnmonitorRemovesImmediateChildObserver() {
    Path dir = Path.from(tmp().createDir("a"));

    tester().monitor();
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    tester().unmonitor();
    assertFalse(service().isMonitored(dir));
    assertFalse(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveImmediateChildObserverThatAreMonitored() {
    Path dir = Path.from(tmp().createDir("a"));

    tester().monitor().monitor(dir.toFile());
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    tester().unmonitor();
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveGrandChildObserver() {
    Path dir = Path.from(tmp().createDir("a/b"));

    tester().monitor().monitor(dir.parent().toFile());
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    tester().unmonitor();
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));
  }
}
