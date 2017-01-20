package l.files.ui.browser;

import org.junit.Test;

import java.util.Locale;

import l.files.fs.Name;
import l.files.fs.Path;

import static java.util.Locale.SIMPLIFIED_CHINESE;
import static l.files.ui.browser.FileSort.NAME;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public final class FileSortNameTest extends FileSortTest {

    @Test
    public void sort_numbers_naturally() throws Exception {
        testSortMatches(
                Locale.getDefault(),
                NAME.comparator(),
                mockFile("0"),
                mockFile("1"),
                mockFile("1."),
                mockFile("1.2"),
                mockFile("1.9"),
                mockFile("1.10"),
                mockFile("2"),
                mockFile("3"),
                mockFile("04"),
                mockFile("05"),
                mockFile("10"),
                mockFile("11"),
                mockFile("21"),
                mockFile("51"),
                mockFile("60"),
                mockFile("99"),
                mockFile("101"),
                mockFile("a1"),
                mockFile("a2"),
                mockFile("a10b"),
                mockFile("a10b1"),
                mockFile("a10b2"),
                mockFile("a10b10")
        );
    }

    @Test
    public void sort_ignores_case() throws Exception {
        testSortMatches(
                Locale.getDefault(),
                NAME.comparator(),
                mockFile("a"),
                mockFile("A"),
                mockFile("b")
        );
    }

    @Test
    public void sort_locale_sensitive() throws Exception {
        testSortMatches(
                SIMPLIFIED_CHINESE,
                NAME.comparator(),
                mockFile("爱"), // Starts with 'a'
                mockFile("你好"), // Starts with 'n'
                mockFile("知道") // Starts with 'z'
        );
    }

    private Path mockFile(String name) {
        Path file = mock(Path.class);
        doReturn(mock(Name.class, name)).when(file).name();
        return file;
    }

}
