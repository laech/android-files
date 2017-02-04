package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.testing.fs.Paths;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.FileSort.MODIFIED;
import static l.files.ui.browser.FileSort.NAME;
import static l.files.ui.browser.FileSort.SIZE;

@RunWith(AndroidJUnit4.class)
public final class SortTest extends BaseFilesActivityTest {

    @Test
    public void updates_list_on_sort_option_change_on_back() throws Exception {
        Path a = dir().concat("a").createDirectory();
        Path aa = createFile("aa", "aa", Instant.of(1, 1), a);
        Path ab = createFile("ab", "ab", Instant.of(2, 1), a);
        Path b = createFile("b", "b", Instant.of(1, 1));
        Path c = createFile("c", "c", Instant.of(6, 1));
        screen()
                .sort().by(NAME).assertAllItemsDisplayedInOrder(a, b, c)
                .clickInto(a)
                .sort().by(NAME).assertAllItemsDisplayedInOrder(aa, ab)
                .sort().by(MODIFIED).assertAllItemsDisplayedInOrder(ab, aa)
                .pressBack().assertAllItemsDisplayedInOrder(a, c, b);
    }

    @Test
    public void updates_list_on_sort_option_change() throws Exception {
        Path a = createFile("a", "a", Instant.of(11, 0));
        Path b = createFile("b", "bbb", Instant.of(12, 0));
        Path c = createFile("c", "cc", Instant.of(13, 0));
        screen()
                .sort().by(NAME).assertAllItemsDisplayedInOrder(a, b, c)
                .sort().by(MODIFIED).assertAllItemsDisplayedInOrder(c, b, a)
                .sort().by(SIZE).assertAllItemsDisplayedInOrder(b, c, a)
                .sort().by(NAME).assertAllItemsDisplayedInOrder(a, b, c);
    }

    private Path createFile(
            String name,
            String content,
            Instant modified) throws IOException {
        return createFile(name, content, modified, dir());
    }

    private Path createFile(
            String name,
            String content,
            Instant modified,
            Path dir) throws IOException {

        Path file = dir.concat(name).createFile();
        Paths.writeUtf8(file, content);
        file.setLastModifiedTime(NOFOLLOW, modified);
        return file;

    }

}
