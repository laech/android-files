package l.files.ui.browser;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.testing.fs.FileBaseTest;

import static java.util.Collections.shuffle;
import static java.util.Collections.sort;
import static l.files.fs.LinkOption.NOFOLLOW;

abstract class FileSortTest extends FileBaseTest {

    protected final void testSortMatches(
            Locale locale,
            Comparator<FileListItem.File> comparator,
            File... expectedOrder) throws IOException {

        List<FileListItem.File> expected = mapData(locale, expectedOrder);
        List<FileListItem.File> actual = new ArrayList<>(expected);
        shuffle(actual);
        sort(actual, comparator);
        assertEquals(expected, actual);
    }

    private List<FileListItem.File> mapData(
            Locale locale,
            File... files) throws IOException {

        Collator collator = Collator.getInstance(locale);
        List<FileListItem.File> expected = new ArrayList<>(files.length);
        for (File file : files) {
            Stat stat;
            try {
                stat = file.stat(NOFOLLOW);
            } catch (IOException e) {
                stat = null;
            }
            expected.add(FileListItem.File.create(file, stat, null, null, collator));
        }
        return expected;
    }

}
