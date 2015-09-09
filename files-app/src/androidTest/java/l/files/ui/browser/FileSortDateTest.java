package l.files.ui.browser;

import java.io.IOException;
import java.util.Locale;

import l.files.fs.File;
import l.files.fs.Instant;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.FileSort.MODIFIED;

public final class FileSortDateTest extends FileSortTest {

    public void test_sort_by_date_desc() throws Exception {
        testSortMatches(
                Locale.getDefault(),
                MODIFIED.comparator(),
                createDirModified("b", Instant.of(1, 3)),
                createFileModified("a", Instant.of(1, 2)),
                createDirModified("c", Instant.of(1, 1)));
    }

    public void test_sort_by_name_if_dates_are_equal() throws Exception {
        testSortMatches(
                Locale.getDefault(),
                MODIFIED.comparator(),
                createFileModified("a", Instant.of(1, 1)),
                createDirModified("b", Instant.of(1, 1)),
                createFileModified("c", Instant.of(1, 1)));
    }

    private File createFileModified(
            final String name,
            final Instant instant) throws IOException {
        final File file = dir1().resolve(name).createFile();
        return setModified(file, instant);
    }

    private File createDirModified(
            final String name,
            final Instant instant) throws IOException {
        final File dir = dir1().resolve(name).createDir();
        return setModified(dir, instant);
    }

    private File setModified(
            final File file,
            final Instant instant) throws IOException {
        file.setLastModifiedTime(NOFOLLOW, instant);
        return file;
    }

}
