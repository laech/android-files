package l.files.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import l.files.common.testing.FileBaseTest;
import l.files.io.file.Path;
import l.files.io.os.ErrnoException;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static java.util.Collections.sort;

abstract class SortByBaseTest extends FileBaseTest {

  protected final void testSortMatches(SortBy sort, File... expectedOrder)
      throws Exception {
    List<FileData> expected = mapData(expectedOrder);
    List<FileData> actual = newArrayList(expected);
    shuffle(actual);
    sort(actual, sort);
    assertEquals(expected, actual);
  }

  private List<FileData> mapData(File... files) throws ErrnoException {
    List<FileData> expected = new ArrayList<>(files.length);
    for (File file : files) {
      expected.add(FileData.stat(Path.from(file)));
    }
    return expected;
  }
}
