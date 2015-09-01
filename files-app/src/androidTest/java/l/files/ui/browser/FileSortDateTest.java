package l.files.ui.browser;

import java.io.IOException;
import java.util.Locale;

import l.files.fs.Instant;
import l.files.fs.Resource;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.FileSort.MODIFIED;

public final class FileSortDateTest extends FileSortTest
{

    public void test_sort_by_date_desc() throws Exception
    {
        testSortMatches(
                Locale.getDefault(),
                MODIFIED.comparator(),
                createDirModified("b", Instant.of(1, 3)),
                createFileModified("a", Instant.of(1, 2)),
                createDirModified("c", Instant.of(1, 1)));
    }

    public void test_sort_by_name_if_dates_are_equal() throws Exception
    {
        testSortMatches(
                Locale.getDefault(),
                MODIFIED.comparator(),
                createFileModified("a", Instant.of(1, 1)),
                createDirModified("b", Instant.of(1, 1)),
                createFileModified("c", Instant.of(1, 1)));
    }

    private Resource createFileModified(
            final String name,
            final Instant instant) throws IOException
    {
        final Resource file = dir1().resolve(name).createFile();
        return setModified(file, instant);
    }

    private Resource createDirModified(
            final String name,
            final Instant instant) throws IOException
    {
        final Resource dir = dir1().resolve(name).createDirectory();
        return setModified(dir, instant);
    }

    private Resource setModified(
            final Resource resource,
            final Instant instant) throws IOException
    {
        resource.setLastModifiedTime(NOFOLLOW, instant);
        return resource;
    }

}
