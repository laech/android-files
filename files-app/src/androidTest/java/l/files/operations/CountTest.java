package l.files.operations;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import l.files.common.testing.FileBaseTest;
import l.files.fs.local.LocalPath;

import static java.util.Arrays.asList;

public final class CountTest extends FileBaseTest {

  public void testCount() throws Exception {
    tmp().createFile("1/a.txt");
    tmp().createFile("3/4/c.txt");

    Set<File> expected = new HashSet<>(asList(
        tmp().get(),
        tmp().get("1"),
        tmp().get("1/a.txt"),
        tmp().get("3"),
        tmp().get("3/4"),
        tmp().get("3/4/c.txt")));

    Count counter = new Count(asList(LocalPath.of(tmp().get())));
    counter.execute();

    assertEquals(expected.size(), counter.getCount());
  }
}
