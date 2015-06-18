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
        final Resource file = directory().resolve(name).createFile();
        try (Writer writer = file.writer(NOFOLLOW, UTF_8))
        {
            writer.write(content);
        }
        file.setModified(NOFOLLOW, modified);
        return file;
    }
}
