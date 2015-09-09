package l.files.ui.browser;

import java.io.IOException;
import java.util.Locale;

import l.files.fs.File;

import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.ui.browser.FileSort.SIZE;

public final class FileSortSizeTest extends FileSortTest {
    public void test_sorts_files_by_size() throws Exception {
        final File smaller = createFile("a", "short content");
        final File larger = createFile("b", "longer content...........");
        testSortMatches(Locale.getDefault(), SIZE.comparator(), larger, smaller);
    }

    public void test_sorts_files_by_name_if_sizes_are_equal() throws Exception {
        final File a = createFile("a", "content a");
        final File b = createFile("b", "content b");
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    public void test_sorts_dirs_by_name_if_sizes_are_equal() throws Exception {
        final File a = dir1().resolve("a").createDir();
        final File b = dir1().resolve("b").createDir();
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    public void test_sorts_dir_last() throws Exception {
        final File f1 = dir1().resolve("a").createFile();
        final File d1 = dir1().resolve("b").createDir();
        final File f2 = dir1().resolve("c").createFile();
        testSortMatches(Locale.getDefault(), SIZE.comparator(), f1, f2, d1);
    }

    public void test_sorts_dir_by_name() throws Exception {
        final File b = dir1().resolve("b").createDir();
        final File a = dir1().resolve("a").createDir();
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    private File createFile(
            final String name,
            final String content) throws IOException {
        final File file = dir1().resolve(name).createFile();
        file.writeString(UTF_8, content);
        return file;
    }
}
