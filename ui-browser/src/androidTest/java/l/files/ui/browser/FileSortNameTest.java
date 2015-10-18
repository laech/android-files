package l.files.ui.browser;

import java.util.Locale;

import static java.util.Locale.SIMPLIFIED_CHINESE;
import static l.files.ui.browser.FileSort.NAME;

public final class FileSortNameTest extends FileSortTest {

    public void test_sort_numbers_naturally() throws Exception {
        testSortMatches(
                Locale.getDefault(),
                NAME.comparator(),
                dir1().resolve("0"),
                dir1().resolve("1"),
                dir1().resolve("1."),
                dir1().resolve("1.2"),
                dir1().resolve("1.9"),
                dir1().resolve("1.10"),
                dir1().resolve("2"),
                dir1().resolve("3"),
                dir1().resolve("04"),
                dir1().resolve("05"),
                dir1().resolve("10"),
                dir1().resolve("11"),
                dir1().resolve("21"),
                dir1().resolve("51"),
                dir1().resolve("60"),
                dir1().resolve("99"),
                dir1().resolve("101"),
                dir1().resolve("a1"),
                dir1().resolve("a2"),
                dir1().resolve("a10b"),
                dir1().resolve("a10b1"),
                dir1().resolve("a10b2"),
                dir1().resolve("a10b10")
        );
    }

    public void test_sort_ignores_case() throws Exception {
        testSortMatches(
                Locale.getDefault(),
                NAME.comparator(),
                dir1().resolve("a"),
                dir1().resolve("A"),
                dir1().resolve("b")
        );
    }

    public void test_sort_locale_sensitive() throws Exception {
        testSortMatches(
                SIMPLIFIED_CHINESE,
                NAME.comparator(),
                dir1().resolve("爱"), // Starts with 'a'
                dir1().resolve("你好"), // Starts with 'n'
                dir1().resolve("知道") // Starts with 'z'
        );
    }
}
