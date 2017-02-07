package l.files.ui.browser;

import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import l.files.fs.LinkOption;
import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.ui.browser.FileSort.SIZE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public final class FileSortSizeTest extends FileSortTest {

    @Test
    public void sorts_files_by_size() throws Exception {
        Path smaller = createFile("a", 2);
        Path larger = createFile("b", 4);
        testSortMatches(Locale.getDefault(), SIZE.comparator(), larger, smaller);
    }

    @Test
    public void sorts_files_by_name_if_sizes_are_equal() throws Exception {
        Path a = createFile("a", 2);
        Path b = createFile("b", 2);
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    @Test
    public void sorts_dirs_by_name_if_sizes_are_equal() throws Exception {
        Path a = createDir("a");
        Path b = createDir("b");
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    @Test
    public void sorts_dir_last() throws Exception {
        Path f1 = createFile("a", 0);
        Path d1 = createDir("b");
        Path f2 = createFile("c", 0);
        testSortMatches(Locale.getDefault(), SIZE.comparator(), f1, f2, d1);
    }

    @Test
    public void sorts_dir_by_name() throws Exception {
        Path b = createDir("b");
        Path a = createDir("a");
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    protected Path createFile(String name, long size) throws IOException {
        Stat stat = mock(Stat.class);
        Path file = mock(Path.class);
        doReturn(size).when(stat).size();
        doReturn(true).when(stat).isRegularFile();
        doReturn(stat).when(file).stat(any(LinkOption.class));
        doReturn(mock(Name.class, name)).when(file).name();
        return file;
    }

    protected Path createDir(String name) throws IOException {
        Stat stat = mock(Stat.class);
        Path file = mock(Path.class);
        doReturn(true).when(stat).isDirectory();
        doReturn(stat).when(file).stat(any(LinkOption.class));
        doReturn(mock(Name.class, name)).when(file).name();
        return file;
    }

}