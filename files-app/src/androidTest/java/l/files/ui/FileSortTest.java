package l.files.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import l.files.common.testing.FileBaseTest;
import l.files.fs.Path;
import l.files.fs.ResourceStatus;
import l.files.fs.local.LocalPath;
import l.files.fs.local.LocalResourceStatus;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static java.util.Collections.sort;

abstract class FileSortTest extends FileBaseTest {

  protected final void testSortMatches(
      Comparator<FileListItem.File> comparator, File... expectedOrder) throws IOException {
    List<FileListItem.File> expected = mapData(expectedOrder);
    ArrayList<FileListItem.File> actual = newArrayList(expected);
    shuffle(actual);
    sort(actual, comparator);
    assertEquals(expected, actual);
  }

  private List<FileListItem.File> mapData(File... files) throws IOException {
    List<FileListItem.File> expected = new ArrayList<>(files.length);
    for (File file : files) {
      Path path = LocalPath.of(file);
      ResourceStatus stat = LocalResourceStatus.stat(path, false);
      expected.add(new FileListItem.File(path, stat, stat));
    }
    return expected;
  }
}
