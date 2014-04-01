package l.files.fse;

import java.io.File;

public final class EventService_UnmonitorTest extends FileEventServiceBaseTest {

  public void testUnmonitorSelf() {
    File dir = tmp().get();

    tester().monitor();
    assertTrue(manager().isMonitored(dir));
    assertTrue(manager().hasObserver(dir));

    tester().unmonitor();
    assertFalse(manager().isMonitored(dir));
    assertFalse(manager().hasObserver(dir));
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
    assertFalse(manager().isMonitored(dir));
    assertTrue(manager().hasObserver(dir));

    tester().unmonitor();
    assertFalse(manager().isMonitored(dir));
    assertFalse(manager().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveImmediateChildObserverThatAreMonitored() {
    File dir = tmp().createDir("a");

    tester().monitor().monitor(dir);
    assertTrue(manager().isMonitored(dir));
    assertTrue(manager().hasObserver(dir));

    tester().unmonitor();
    assertTrue(manager().isMonitored(dir));
    assertTrue(manager().hasObserver(dir));
  }

  public void testUnmonitorDoesNotRemoveGrandChildObserver() {
    File dir = tmp().createDir("a/b");

    tester().monitor().monitor(dir.getParentFile());
    assertFalse(manager().isMonitored(dir));
    assertTrue(manager().hasObserver(dir));

    tester().unmonitor();
    assertFalse(manager().isMonitored(dir));
    assertTrue(manager().hasObserver(dir));
  }
}
