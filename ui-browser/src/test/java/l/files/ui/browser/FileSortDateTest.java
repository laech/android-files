package l.files.ui.browser;

import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import l.files.fs.File;
import l.files.fs.FileName;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Stat;

import static l.files.ui.browser.FileSort.MODIFIED;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public final class FileSortDateTest extends FileSortTest {

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

    private File createFileModified(String name, Instant instant) throws IOException {
        return createModified(name, instant, false);
    }

    private File createDirModified(String name, Instant instant) throws IOException {
        return createModified(name, instant, true);
    }

    private File createModified(String name, Instant instant, boolean dir) throws IOException {
        Stat stat = mock(Stat.class);
        File file = mock(File.class);
        given(stat.lastModifiedTime()).willReturn(instant);
        given(stat.isDirectory()).willReturn(dir);
        given(stat.isRegularFile()).willReturn(!dir);
        given(file.stat(any(LinkOption.class))).willReturn(stat);
        given(file.name()).willReturn(FileName.of(name));
        return file;
    }

}