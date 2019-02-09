package l.files.ui.browser.sort;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;

import static java.util.Collections.shuffle;
import static java.util.Collections.sort;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;

abstract class FileSortTest {

    final void testSortMatches(
            Locale locale,
            Comparator<FileInfo> comparator,
            Path... expectedOrder) throws IOException {

        List<FileInfo> expected = mapData(locale, expectedOrder);
        List<FileInfo> actual = new ArrayList<>(expected);
        shuffle(actual);
        sort(actual, comparator);
        assertEquals(names(expected), names(actual));
        assertEquals(expected, actual);
    }

    private List<Name> names(List<FileInfo> items) {
        List<Name> names = new ArrayList<>(items.size());
        for (FileInfo item : items) {
            names.add(item.selfPath().name());
        }
        return names;
    }

    private List<FileInfo> mapData(Locale locale, Path... files)
            throws IOException {

        Collator collator = Collator.getInstance(locale);
        List<FileInfo> expected = new ArrayList<>(files.length);
        for (Path file : files) {
            Stat stat;
            try {
                stat = file.stat(NOFOLLOW);
            } catch (IOException e) {
                stat = null;
            }
            expected.add(FileInfo.create(file, stat, null, null, collator));
        }
        return expected;
    }

}