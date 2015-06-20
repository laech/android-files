package l.files.ui.browser;

import java.io.IOException;
import java.util.Locale;

import l.files.fs.Resource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.FileSort.SIZE;

public final class FileSortSizeTest extends FileSortTest
{
    public void test_sorts_files_by_size() throws Exception
    {
        final Resource smaller = createFile("a", "short content");
        final Resource larger = createFile("b", "longer content...........");
        testSortMatches(Locale.getDefault(), SIZE.comparator(), larger, smaller);
    }

    public void test_sorts_files_by_name_if_sizes_are_equal() throws Exception
    {
        final Resource a = createFile("a", "content a");
        final Resource b = createFile("b", "content b");
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    public void test_sorts_dirs_by_name_if_sizes_are_equal() throws Exception
    {
        final Resource a = dir1().resolve("a").createDirectory();
        final Resource b = dir1().resolve("b").createDirectory();
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    public void test_sorts_dir_last() throws Exception
    {
        final Resource f1 = dir1().resolve("a").createFile();
        final Resource d1 = dir1().resolve("b").createDirectory();
        final Resource f2 = dir1().resolve("c").createFile();
        testSortMatches(Locale.getDefault(), SIZE.comparator(), f1, f2, d1);
    }

    public void test_sorts_dir_by_name() throws Exception
    {
        final Resource b = dir1().resolve("b").createDirectory();
        final Resource a = dir1().resolve("a").createDirectory();
        testSortMatches(Locale.getDefault(), SIZE.comparator(), a, b);
    }

    private Resource createFile(
            final String name,
            final String content) throws IOException
    {
        final Resource file = dir1().resolve(name).createFile();
        file.writeString(NOFOLLOW, UTF_8, content);
        return file;
    }
}
