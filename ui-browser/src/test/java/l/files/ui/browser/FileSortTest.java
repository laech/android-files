package l.files.ui.browser;

import com.ibm.icu.text.Collator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import l.files.fs.Files;
import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.text.Collators;

import static java.util.Collections.shuffle;
import static java.util.Collections.sort;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;

abstract class FileSortTest {

    protected final void testSortMatches(
            Locale locale,
            Comparator<FileItem> comparator,
            Path... expectedOrder) throws IOException {

        List<FileItem> expected = mapData(locale, expectedOrder);
        List<FileItem> actual = new ArrayList<>(expected);
        shuffle(actual);
        sort(actual, comparator);
        assertEquals(names(expected), names(actual));
        assertEquals(expected, actual);
    }

    private List<Name> names(List<FileItem> items) {
        List<Name> names = new ArrayList<>(items.size());
        for (FileItem item : items) {
            names.add(item.selfPath().name());
        }
        return names;
    }

    private List<FileItem> mapData(
            Locale locale,
            Path... files) throws IOException {

        final Collator collator = Collators.of(locale);
        final List<FileItem> expected = new ArrayList<>(files.length);
        for (Path file : files) {
            Stat stat;
            try {
                stat = Files.stat(file, NOFOLLOW);
            } catch (IOException e) {
                stat = null;
            }
            expected.add(FileItem.create(file, stat, null, null, new Provider<Collator>() {
                @Override
                public Collator get() {
                    return collator;
                }
            }));
        }
        return expected;
    }

}
