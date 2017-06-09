package l.files.ui.browser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Locale;

import l.files.fs.Path;
import l.files.testing.fs.Paths;

import static l.files.ui.browser.FileSort.SIZE;

public final class FileSortSizeTest extends FileSortTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

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

    private Path createFile(String name, int size) throws IOException {
        if (size > 10) {
            throw new IllegalArgumentException("size to big: " + size);
        }
        Path path = Path.of(temporaryFolder.newFile(name));
        Paths.writeUtf8(path, repeat("a", size));
        return path;
    }

    private String repeat(String s, int n) {
        StringBuilder builder = new StringBuilder(s.length() * n);
        for (int i = 0; i < n; i++) {
            builder.append(s);
        }
        return builder.toString();
    }

    private Path createDir(String name) throws IOException {
        return Path.of(temporaryFolder.newFolder(name));
    }

}
