package l.files.ui.browser;

import org.junit.Test;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Instant;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.FileSort.MODIFIED;
import static l.files.ui.browser.FileSort.NAME;
import static l.files.ui.browser.FileSort.SIZE;

public final class SortTest extends BaseFilesActivityTest {

    @Test
    public void updates_list_on_sort_option_change_on_back() throws Exception {
        File a = dir().resolve("a").createDir();
        File aa = createFile("aa", "aa", Instant.of(1, 1), a);
        File ab = createFile("ab", "ab", Instant.of(2, 1), a);
        File b = createFile("b", "b", Instant.of(1, 1));
        File c = createFile("c", "c", Instant.of(6, 1));
        screen()
                .sort().by(NAME).assertItemsDisplayed(a, b, c)
                .clickInto(a)
                .sort().by(NAME).assertItemsDisplayed(aa, ab)
                .sort().by(MODIFIED).assertItemsDisplayed(ab, aa)
                .pressBack().assertItemsDisplayed(a, c, b);
    }

    @Test
    public void updates_list_on_sort_option_change() throws Exception {
        File a = createFile("a", "a", Instant.of(1, 1));
        File b = createFile("b", "bbb", Instant.of(1, 2));
        File c = createFile("c", "cc", Instant.of(1, 3));
        screen()
                .sort().by(NAME).assertItemsDisplayed(a, b, c)
                .sort().by(MODIFIED).assertItemsDisplayed(c, b, a)
                .sort().by(SIZE).assertItemsDisplayed(b, c, a)
                .sort().by(NAME).assertItemsDisplayed(a, b, c);
    }

    private File createFile(
            String name,
            String content,
            Instant modified) throws IOException {
        return createFile(name, content, modified, dir());
    }

    private File createFile(
            String name,
            String content,
            Instant modified,
            File dir) throws IOException {

        File file = dir.resolve(name).createFile();
        file.writeAllUtf8(content);
        file.setLastModifiedTime(NOFOLLOW, modified);
        return file;

    }

}
