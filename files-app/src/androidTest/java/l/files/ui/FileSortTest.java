package l.files.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import l.files.common.testing.FileBaseTest;
import l.files.fs.FileStatus;
import l.files.fs.local.LocalFileStatus;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static java.util.Collections.sort;

abstract class FileSortTest extends FileBaseTest {

  protected final void testSortMatches(
      Comparator<FileStatus> comparator, File... expectedOrder) {
    List<LocalFileStatus> expected = mapData(expectedOrder);
    List<LocalFileStatus> actual = newArrayList(expected);
    shuffle(actual);
    sort(actual, comparator);
    assertEquals(expected, actual);
  }

  private List<LocalFileStatus> mapData(File... files) {
    List<LocalFileStatus> expected = new ArrayList<>(files.length);
    for (File file : files) {
      expected.add(LocalFileStatus.read(file.getPath()));
    }
    return expected;
  }
}
