package l.files.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import l.files.common.testing.FileBaseTest;
import l.files.fs.local.FileInfo;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static java.util.Collections.sort;

abstract class FileSortTest extends FileBaseTest {

  protected final void testSortMatches(FileSort sort, File... expectedOrder)
      throws Exception {
    List<FileInfo> expected = mapData(expectedOrder);
    List<FileInfo> actual = newArrayList(expected);
    shuffle(actual);
    sort(actual, sort);
    assertEquals(expected, actual);
  }

  private List<FileInfo> mapData(File... files) throws IOException {
    List<FileInfo> expected = new ArrayList<>(files.length);
    for (File file : files) {
      expected.add(FileInfo.read(file.getPath()));
    }
    return expected;
  }
}
