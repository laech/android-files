package l.files.ui.browser;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.fs.local.ResourceBaseTest;
import l.files.ui.browser.FileListItem.File;

import static java.util.Collections.shuffle;
import static java.util.Collections.sort;
import static l.files.fs.LinkOption.NOFOLLOW;

abstract class FileSortTest extends ResourceBaseTest {

  protected final void testSortMatches(
      Locale locale,
      Comparator<File> comparator,
      Resource... expectedOrder) throws IOException {

    List<File> expected = mapData(locale, expectedOrder);
    List<File> actual = new ArrayList<>(expected);
    shuffle(actual);
    sort(actual, comparator);
    assertEquals(expected, actual);
  }

  private List<File> mapData(
      Locale locale,
      Resource... resources) throws IOException {

    Collator collator = Collator.getInstance(locale);
    List<File> expected = new ArrayList<>(resources.length);
    for (Resource resource : resources) {
      Stat stat;
      try {
        stat = resource.stat(NOFOLLOW);
      } catch (IOException e) {
        stat = null;
      }
      expected.add(File.create(resource, stat, stat, collator));
    }
    return expected;
  }

}
