package l.files.ui.browser.sort;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Locale;

import l.files.fs.Instant;
import l.files.fs.Path;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.sort.FileSort.MODIFIED;

public final class FileSortDateTest extends FileSortTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void sort_by_date_desc() throws Exception {
        testSortMatches(
                Locale.getDefault(),
                MODIFIED.comparator(),
                createDirModified("b", Instant.of(1, 3)),
                createFileModified("a", Instant.of(1, 2)),
                createDirModified("c", Instant.of(1, 1)));
    }

    @Test
    public void sort_by_name_if_dates_are_equal() throws Exception {
        testSortMatches(
                Locale.getDefault(),
                MODIFIED.comparator(),
                createFileModified("a", Instant.of(1, 1)),
                createDirModified("b", Instant.of(1, 1)),
                createFileModified("c", Instant.of(1, 1)));
    }

    private Path createFileModified(String name, Instant instant) throws IOException {
        return createModified(name, instant, false);
    }

    private Path createDirModified(String name, Instant instant) throws IOException {
        return createModified(name, instant, true);
    }

    private Path createModified(String name, Instant instant, boolean dir) throws IOException {
        Path path;
        if (dir) {
            path = Path.of(temporaryFolder.newFolder(name));
        } else {
            path = Path.of(temporaryFolder.newFile(name));
        }
        path.setLastModifiedTime(NOFOLLOW, instant);
        return path;
    }

}
