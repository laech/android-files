package l.files.ui.browser;

import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import l.files.fs.File;
import l.files.fs.LinkOption;
import l.files.fs.Name;
import l.files.fs.Stat;

import static l.files.ui.browser.FileSort.SIZE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public final class FileSortSizeTest extends FileSortTest {

    @Test
    public void sorts_files_by_size() throws Exception {
        File smaller = createFile("a", 2);
        File larger = createFile("b", 4);
        testSortMatches(Locale.getDefault(), SIZE.comparator(), larger, smaller);
    }

    @Test
    public void sorts_files_by_name_if_sizes_are_equal() throws Exception {
        File a = createFile("a", 2);
        File b = createFile("b", 2);
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    @Test
    public void sorts_dirs_by_name_if_sizes_are_equal() throws Exception {
        File a = createDir("a");
        File b = createDir("b");
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    @Test
    public void sorts_dir_last() throws Exception {
        File f1 = createFile("a", 0);
        File d1 = createDir("b");
        File f2 = createFile("c", 0);
        testSortMatches(Locale.getDefault(), SIZE.comparator(), f1, f2, d1);
    }

    @Test
    public void sorts_dir_by_name() throws Exception {
        File b = createDir("b");
        File a = createDir("a");
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    protected File createFile(String nameStr, long size) throws IOException {
        Stat stat = mock(Stat.class);
        File file = mock(File.class);
        Name name = mock(Name.class);
        given(name.toString()).willReturn(nameStr);
        given(stat.size()).willReturn(size);
        given(stat.isRegularFile()).willReturn(true);
        given(file.stat(any(LinkOption.class))).willReturn(stat);
        given(file.name()).willReturn(name);
        return file;
    }

    protected File createDir(String nameStr) throws IOException {
        Stat stat = mock(Stat.class);
        File file = mock(File.class);
        Name name = mock(Name.class);
        given(name.toString()).willReturn(nameStr);
        given(stat.isDirectory()).willReturn(true);
        given(file.stat(any(LinkOption.class))).willReturn(stat);
        given(file.name()).willReturn(name);
        return file;
    }

}
