package l.files.ui.browser.sort;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Locale;

import static java.nio.file.Files.setLastModifiedTime;
import static l.files.ui.browser.sort.FileSort.MODIFIED;

public final class FileSortDateTest extends FileSortTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void sort_by_date_desc() throws Exception {
        testSortMatches(
            Locale.getDefault(),
            MODIFIED.comparator(),
            createDirModified("b", Instant.ofEpochSecond(3)),
            createFileModified("a", Instant.ofEpochSecond(2)),
            createDirModified("c", Instant.ofEpochSecond(1))
        );
    }

    @Test
    public void sort_by_name_if_dates_are_equal() throws Exception {
        testSortMatches(
            Locale.getDefault(),
            MODIFIED.comparator(),
            createFileModified("a", Instant.ofEpochSecond(1)),
            createDirModified("b", Instant.ofEpochSecond(1)),
            createFileModified("c", Instant.ofEpochSecond(1))
        );
    }

    private Path createFileModified(String name, Instant instant)
        throws IOException {
        return createModified(name, instant, false);
    }

    private Path createDirModified(String name, Instant instant)
        throws IOException {
        return createModified(name, instant, true);
    }

    private Path createModified(String name, Instant instant, boolean dir)
        throws IOException {
        Path path;
        if (dir) {
            path = temporaryFolder.newFolder(name).toPath();
        } else {
            path = temporaryFolder.newFile(name).toPath();
        }
        setLastModifiedTime(path, FileTime.from(instant));
        return path;
    }

}
