package l.files.ui.browser;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.*;
import static l.files.ui.browser.sort.FileSort.*;

public final class SortTest extends BaseFilesActivityTest {

    @Test
    public void updates_list_on_sort_option_change_on_back() throws Exception {
        Path a = createDirectory(dir().resolve("a"));
        Path aa = createFile("aa", "aa", Instant.ofEpochSecond(1, 1), a);
        Path ab = createFile("ab", "ab", Instant.ofEpochSecond(2, 1), a);
        Path b = createFile("b", "b", Instant.ofEpochSecond(1, 1));
        Path c = createFile("c", "c", Instant.ofEpochSecond(6, 1));
        screen()
            .sort().by(NAME)
            .assertAllItemsDisplayedInOrder(a, b, c)

            .clickInto(a)
            .sort().by(NAME)
            .assertAllItemsDisplayedInOrder(aa, ab)

            .sort().by(MODIFIED)
            .assertAllItemsDisplayedInOrder(ab, aa)

            .pressBack()
            .assertCurrentDirectory(dir())
            .assertAllItemsDisplayedInOrder(a, c, b);
    }

    @Test
    public void updates_list_on_sort_option_change() throws Exception {
        Path a = createFile("a", "a", Instant.ofEpochSecond(11, 0));
        Path b = createFile("b", "bbb", Instant.ofEpochSecond(12, 0));
        Path c = createFile("c", "cc", Instant.ofEpochSecond(13, 0));
        screen()
            .sort().by(NAME).assertAllItemsDisplayedInOrder(a, b, c)
            .sort().by(MODIFIED).assertAllItemsDisplayedInOrder(c, b, a)
            .sort().by(SIZE).assertAllItemsDisplayedInOrder(b, c, a)
            .sort().by(NAME).assertAllItemsDisplayedInOrder(a, b, c);
    }

    private Path createFile(
        String name,
        String content,
        Instant modified
    ) throws IOException {
        return createFile(name, content, modified, dir());
    }

    private Path createFile(
        String name,
        String content,
        Instant modified,
        Path dir
    ) throws IOException {
        Path file = Files.createFile(dir.resolve(name));
        write(file, content.getBytes(UTF_8));
        setLastModifiedTime(file, FileTime.from(modified));
        return file;

    }

}
