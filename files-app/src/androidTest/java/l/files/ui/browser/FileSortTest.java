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

abstract class FileSortTest extends ResourceBaseTest
{
    protected final void testSortMatches(
            final Locale locale,
            final Comparator<File> comparator,
            final Resource... expectedOrder) throws IOException
    {
        final List<File> expected = mapData(locale, expectedOrder);
        final ArrayList<File> actual = new ArrayList<>(expected);
        shuffle(actual);
        sort(actual, comparator);
        assertEquals(expected, actual);
    }

    private List<File> mapData(
            final Locale locale,
            final Resource... resources) throws IOException
    {
        final Collator collator = Collator.getInstance(locale);
        final List<File> expected = new ArrayList<>(resources.length);
        for (final Resource resource : resources)
        {
            final Stat stat = resource.stat(NOFOLLOW);
            expected.add(File.create(resource, stat, stat, collator));
        }
        return expected;
    }
}
