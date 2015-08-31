package l.files.features;

import java.io.IOException;
import java.io.Writer;

import l.files.fs.Instant;
import l.files.fs.Resource;
import l.files.test.BaseFilesActivityTest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.FileSort.MODIFIED;
import static l.files.ui.browser.FileSort.NAME;
import static l.files.ui.browser.FileSort.SIZE;

public final class SortTest extends BaseFilesActivityTest
{
    public void test_updates_list_on_sort_option_change_on_back() throws Exception
    {
        final Resource a = dir().resolve("a").createDirectory();
        final Resource aa = createFile("aa", "aa", Instant.of(1, 1), a);
        final Resource ab = createFile("ab", "ab", Instant.of(2, 1), a);
        final Resource b = createFile("b", "b", Instant.of(1, 1));
        final Resource c = createFile("c", "c", Instant.of(6, 1));
        screen()
                .sort().by(NAME).assertItemsDisplayed(a, b, c)
                .clickInto(a)
                .sort().by(NAME).assertItemsDisplayed(aa, ab)
                .sort().by(MODIFIED).assertItemsDisplayed(ab, aa)
                .pressBack().assertItemsDisplayed(a, b, c);
    }

    public void test_updates_list_on_sort_option_change() throws Exception
    {
        final Resource a = createFile("a", "a", Instant.of(1, 1));
        final Resource b = createFile("b", "bbb", Instant.of(1, 2));
        final Resource c = createFile("c", "cc", Instant.of(1, 3));
        screen()
                .sort().by(NAME).assertItemsDisplayed(a, b, c)
                .sort().by(MODIFIED).assertItemsDisplayed(c, b, a)
                .sort().by(SIZE).assertItemsDisplayed(b, c, a)
                .sort().by(NAME).assertItemsDisplayed(a, b, c);
    }

    private Resource createFile(
            final String name,
            final String content,
            final Instant modified) throws IOException
    {
        return createFile(name, content, modified, dir());
    }

    private Resource createFile(
            final String name,
            final String content,
            final Instant modified,
            final Resource dir) throws IOException
    {
        final Resource file = dir.resolve(name).createFile();
        try (Writer writer = file.writer(UTF_8))
        {
            writer.write(content);
        }
        file.setModified(NOFOLLOW, modified);
        return file;
    }
}
