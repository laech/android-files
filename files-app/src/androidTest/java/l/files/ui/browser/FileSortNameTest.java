package l.files.ui.browser;

import java.util.Locale;

import static java.util.Locale.SIMPLIFIED_CHINESE;

public final class FileSortNameTest extends FileSortTest {

    public void testIgnoresCase() throws Exception {
        testSortMatches(FileSort.NAME.newComparator(Locale.getDefault()),
                dir1().resolve("a").createFile(),
                dir1().resolve("A").createDirectory(),
                dir1().resolve("b").createFile()
        );
    }

    public void testLocaleSensitive() throws Exception {
        testSortMatches(FileSort.NAME.newComparator(SIMPLIFIED_CHINESE),
                dir1().resolve("爱").createFile(), // Starts with 'a'
                dir1().resolve("你好").createFile(), // Starts with 'n'
                dir1().resolve("知道").createFile() // Starts with 'z'
        );
    }
}
