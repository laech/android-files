package l.files.ui.browser.sort;

import org.junit.Test;

import java.util.Locale;

import l.files.fs.Path;

import static java.util.Locale.SIMPLIFIED_CHINESE;
import static l.files.ui.browser.sort.FileSort.NAME;

public final class FileSortNameTest extends FileSortTest {

    @Test
    public void sort_numbers_naturally() throws Exception {
        testSortMatches(
                Locale.getDefault(),
                NAME.comparator(),
                Path.of("0"),
                Path.of("1"),
                Path.of("1."),
                Path.of("1.2"),
                Path.of("1.9"),
                Path.of("1.10"),
                Path.of("2"),
                Path.of("3"),
                Path.of("04"),
                Path.of("05"),
                Path.of("10"),
                Path.of("11"),
                Path.of("21"),
                Path.of("51"),
                Path.of("60"),
                Path.of("99"),
                Path.of("101"),
                Path.of("a1"),
                Path.of("a2"),
                Path.of("a10b"),
                Path.of("a10b1"),
                Path.of("a10b2"),
                Path.of("a10b10")
        );
    }

    @Test
    public void sort_ignores_case() throws Exception {
        testSortMatches(
                Locale.getDefault(),
                NAME.comparator(),
                Path.of("a"),
                Path.of("A"),
                Path.of("b")
        );
    }

    @Test
    public void sort_locale_sensitive() throws Exception {
        testSortMatches(
                SIMPLIFIED_CHINESE,
                NAME.comparator(),
                Path.of("爱"), // Starts with 'a'
                Path.of("你好"), // Starts with 'n'
                Path.of("知道") // Starts with 'z'
        );
    }

}
