package l.files.ui.browser;

import com.ibm.icu.text.Collator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import l.files.collation.NaturalKey;
import l.files.fs.File;
import l.files.fs.FileName;
import l.files.fs.Stat;
import l.files.testing.fs.FileBaseTest;
import l.files.ui.browser.BrowserItem.FileItem;

import static java.util.Collections.shuffle;
import static java.util.Collections.sort;
import static l.files.fs.LinkOption.NOFOLLOW;

abstract class FileSortTest extends FileBaseTest {

    protected final void testSortMatches(
            Locale locale,
            Comparator<FileItem> comparator,
            File... expectedOrder) throws IOException {

        List<FileItem> expected = mapData(locale, expectedOrder);
        List<FileItem> actual = new ArrayList<>(expected);
        shuffle(actual);
        sort(actual, comparator);
        assertEquals(names(expected), names(actual));
        assertEquals(expected, actual);
    }

    private List<FileName> names(List<FileItem> items) {
        List<FileName> names = new ArrayList<>(items.size());
        for (FileItem item : items) {
            names.add(item.selfFile().name());
        }
        return names;
    }

    private List<FileItem> mapData(
            Locale locale,
            File... files) throws IOException {

        Collator collator = NaturalKey.collator(locale);
        List<FileItem> expected = new ArrayList<>(files.length);
        for (File file : files) {
            Stat stat;
            try {
                stat = file.stat(NOFOLLOW);
            } catch (IOException e) {
                stat = null;
            }
            expected.add(FileItem.create(file, stat, null, null, collator));
        }
        return expected;
    }

}
