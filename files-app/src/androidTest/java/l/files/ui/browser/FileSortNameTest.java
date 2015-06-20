package l.files.ui.browser;

import java.util.Locale;

import static java.util.Locale.SIMPLIFIED_CHINESE;
import static l.files.ui.browser.FileSort.NAME;

public final class FileSortNameTest extends FileSortTest
{
    public void test_sort_ignores_case() throws Exception
    {
        testSortMatches(
                Locale.getDefault(),
                NAME.comparator(),
                dir1().resolve("a").createFile(),
                dir1().resolve("A").createDirectory(),
                dir1().resolve("b").createFile()
        );
    }

    public void test_sort_locale_sensitive() throws Exception
    {
        testSortMatches(
                SIMPLIFIED_CHINESE,
                NAME.comparator(),
                dir1().resolve("爱").createFile(), // Starts with 'a'
                dir1().resolve("你好").createFile(), // Starts with 'n'
                dir1().resolve("知道").createFile() // Starts with 'z'
        );
    }
}
