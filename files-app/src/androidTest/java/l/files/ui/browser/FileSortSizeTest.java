package l.files.ui.browser;

import java.io.IOException;
import java.util.Locale;

import l.files.fs.Resource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class FileSortSizeTest extends FileSortTest {

    public void testSortsFilesBySize() throws Exception {
        Resource smaller = createFile("a", "short content");
        Resource larger = createFile("b", "longer content...........");
        testSortMatches(FileSort.SIZE.newComparator(Locale.getDefault()), larger, smaller);
    }

    public void testSortsFilesByNameIfSizesEqual() throws Exception {
        Resource a = createFile("a", "content a");
        Resource b = createFile("b", "content b");
        testSortMatches(FileSort.SIZE.newComparator(Locale.getDefault()), a, b);
    }

    public void testSortsDirsByNameIfSizesEqual() throws Exception {
        Resource a = dir1().resolve("a").createDirectory();
        Resource b = dir1().resolve("b").createDirectory();
        testSortMatches(FileSort.SIZE.newComparator(Locale.getDefault()), a, b);
    }

    public void testSortsDirLast() throws Exception {
        Resource f1 = dir1().resolve("a").createFile();
        Resource d1 = dir1().resolve("b").createDirectory();
        Resource f2 = dir1().resolve("c").createFile();
        testSortMatches(FileSort.SIZE.newComparator(Locale.getDefault()), f1, f2, d1);
    }

    public void testSortsDirByName() throws Exception {
        Resource b = dir1().resolve("b").createDirectory();
        Resource a = dir1().resolve("a").createDirectory();
        testSortMatches(FileSort.SIZE.newComparator(Locale.getDefault()), a, b);
    }

    private Resource createFile(String name, String content) throws IOException {
        Resource file = dir1().resolve(name).createFile();
        file.writeString(NOFOLLOW, UTF_8, content);
        return file;
    }

}
