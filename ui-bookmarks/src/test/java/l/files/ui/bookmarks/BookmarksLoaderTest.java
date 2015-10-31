package l.files.ui.bookmarks;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l.files.bookmarks.BookmarkManager;
import l.files.fs.File;
import l.files.fs.FileName;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class BookmarksLoaderTest {

    private BookmarkManager bookmarks;
    private BookmarksLoader loader;
    private File home;

    @Before
    public void setUp() throws Exception {
        Context context = mock(Context.class);
        given(context.getApplicationContext()).willReturn(context);

        home = mock(File.class);
        given(home.name()).willReturn(FileName.of("home"));
        bookmarks = mock(BookmarkManager.class);
        loader = new BookmarksLoader(context, bookmarks, home);
    }

    @Test
    public void sorts_bookmarks_by_name() throws Exception {
        File a = mockFile("a");
        File b = mockFile("b");
        File c = mockFile("c");
        File d = mockFile("d");
        File e = mockFile("e");

        given(bookmarks.getBookmarks()).willReturn(asSet(a, c, b, e, d));
        List<File> expected = asList(a, b, c, d, e);
        List<File> actual = loader.loadInBackground();
        assertEquals(expected, actual);
    }

    @Test
    public void sorts_home_at_top() throws Exception {
        File a = mockFile("a");
        File z = mockFile("z");

        given(bookmarks.getBookmarks()).willReturn(asSet(a, z, home));
        List<File> expected = asList(home, a, z);
        List<File> actual = loader.loadInBackground();
        assertEquals(expected, actual);
    }

    private Set<File> asSet(File... files) {
        return unmodifiableSet(new HashSet<>(asList(files)));
    }

    private File mockFile(String name) {
        File file = mock(File.class, name);
        given(file.name()).willReturn(FileName.of(name));
        return file;
    }

}
