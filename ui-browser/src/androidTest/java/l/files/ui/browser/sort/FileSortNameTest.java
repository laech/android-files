package l.files.ui.browser.sort;

import org.junit.Test;

import java.nio.file.Paths;
import java.util.Locale;

import static java.util.Locale.SIMPLIFIED_CHINESE;
import static l.files.ui.browser.sort.FileSort.NAME;

public final class FileSortNameTest extends FileSortTest {

    @Test
    public void sort_numbers_naturally() {
        testSortMatches(
            Locale.getDefault(),
            NAME.comparator(),
            Paths.get("0"),
            Paths.get("1"),
            Paths.get("1."),
            Paths.get("1.2"),
            Paths.get("1.9"),
            Paths.get("1.10"),
            Paths.get("2"),
            Paths.get("3"),
            Paths.get("04"),
            Paths.get("05"),
            Paths.get("10"),
            Paths.get("11"),
            Paths.get("21"),
            Paths.get("51"),
            Paths.get("60"),
            Paths.get("99"),
            Paths.get("101"),
            Paths.get("a1"),
            Paths.get("a2"),
            Paths.get("a10b"),
            Paths.get("a10b1"),
            Paths.get("a10b2"),
            Paths.get("a10b10")
        );
    }

    @Test
    public void sort_ignores_case() {
        testSortMatches(
            Locale.getDefault(),
            NAME.comparator(),
            Paths.get("a"),
            Paths.get("A"),
            Paths.get("b")
        );
    }

    @Test
    public void sort_locale_sensitive() {
        testSortMatches(
            SIMPLIFIED_CHINESE,
            NAME.comparator(),
            Paths.get("爱"), // Starts with 'a'
            Paths.get("你好"), // Starts with 'n'
            Paths.get("知道") // Starts with 'z'
        );
    }

}
