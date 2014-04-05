package l.files.fse;

import java.io.File;

public final class FileEventService_UnmonitorTest extends FileEventServiceBaseTest {

  public void testUnmonitorRootDirChildren() {
    service().monitor2(new File("/"));
    assertTrue(service().toString(), service().hasObserver(new File("/dev")));
    assertTrue(service().toString(), service().hasObserver(new File("/data")));

    service().unmonitor(new File("/"));
    assertFalse(service().toString(), service().hasObserver(new File("/dev")));
    assertFalse(service().toString(), service().hasObserver(new File("/data")));
  }

  public void testUnmonitorSelf() {
    File dir = tmp().get();

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
    File dir = tmp().createDir("a");

    tester().monitor();
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    tester().unmonitor();
    assertFalse(service().isMonitored(dir));
    assertFalse(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveImmediateChildObserverThatAreMonitored() {
    File dir = tmp().createDir("a");

    tester().monitor().monitor(dir);
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    tester().unmonitor();
    assertTrue(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveGrandChildObserver() {
    File dir = tmp().createDir("a/b");

    tester().monitor().monitor(dir.getParentFile());
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));

    tester().unmonitor();
    assertFalse(service().isMonitored(dir));
    assertTrue(service().hasObserver(dir));
  }
}
