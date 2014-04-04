package l.files.fse;

import java.io.File;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.os.Stat.stat;

public final class EventService_MonitorTest extends FileEventServiceBaseTest {

  public void testMonitorReturnsChildrenInfo() throws Exception {
    File a = tmp().createDir("a");
    File b = tmp().createFile("b");
    File c = tmp().createFile("c");

    Set<PathStat> expected = newHashSet(
        PathStat.create(a.getPath(), stat(a.getPath())),
        PathStat.create(b.getPath(), stat(b.getPath())),
        PathStat.create(c.getPath(), stat(c.getPath())));

    List<PathStat> result = service().monitor2(tmp().get()).get();
    Set<PathStat> actual = newHashSet(result);

    assertEquals(expected, actual);
    assertEquals(expected.size(), result.size());
  }
}
