package l.files.ui.bookmarks;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l.files.bookmarks.BookmarkManager;
import l.files.fs.Name;
import l.files.fs.Path;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public final class BookmarksLoaderTest {

    private BookmarkManager bookmarks;
    private BookmarksLoader loader;
    private Path home;

    @Before
    public void setUp() throws Exception {
        Context context = mock(Context.class);
        given(context.getApplicationContext()).willReturn(context);

        home = mock(Path.class);
        doReturn(mock(Name.class, "a")).when(home).name();
        bookmarks = mock(BookmarkManager.class);
        loader = new BookmarksLoader(context, bookmarks, home);
    }

    @Test
    public void sorts_bookmarks_by_name() throws Exception {
        Path a = mockFile("a");
        Path b = mockFile("b");
        Path c = mockFile("c");
        Path d = mockFile("d");
        Path e = mockFile("e");

        given(bookmarks.getBookmarks()).willReturn(asSet(a, c, b, e, d));
        List<Path> expected = asList(a, b, c, d, e);
        List<Path> actual = loader.loadInBackground();
        assertEquals(expected, actual);
    }

    @Test
    public void sorts_home_at_top() throws Exception {
        Path a = mockFile("a");
        Path z = mockFile("z");

        given(bookmarks.getBookmarks()).willReturn(asSet(a, z, home));
        List<Path> expected = asList(home, a, z);
        List<Path> actual = loader.loadInBackground();
        assertEquals(expected, actual);
    }

    private Set<Path> asSet(Path... files) {
        return unmodifiableSet(new HashSet<>(asList(files)));
    }

    private Path mockFile(String name) {
        Path file = mock(Path.class);
        doReturn(mock(Name.class, name)).when(file).name();
        return file;
    }

}
