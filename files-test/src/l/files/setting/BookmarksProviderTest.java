package l.files.setting;

import android.content.SharedPreferences;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import junit.framework.TestCase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Set;

import static android.content.SharedPreferences.Editor;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.*;

public final class BookmarksProviderTest extends TestCase {

  public static final String KEY = "bookmarks";

  private SharedPreferences pref;
  private Editor editor;
  private Set<String> defaults;

  private BookmarksProvider handler;

  @Override protected void setUp() throws Exception {
    super.setUp();
    editor = mockEditor();
    pref = mockSharedPreferences(editor);
    defaults = ImmutableSet.of("/");
    handler = new BookmarksProvider(defaults);
    handler.register(mock(Bus.class), pref);
  }

  public void testAddBookmarkRequestIsHandled() {
    bookmark("/a");
    handler.handle(new AddBookmarkRequest(new File("/")));
    verify(editor).putStringSet(KEY, newHashSet("/", "/a"));
    verify(editor).apply();
  }

  public void testAddBookmarkRequestHandlerMethodIsConfigured() throws Exception {
    Method method = BookmarksProvider.class.getMethod("handle", AddBookmarkRequest.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testRemoveBookmarkRequestIsHandled() {
    bookmark("/a", "/");
    handler.handle(new RemoveBookmarkRequest(new File("/")));
    verify(editor).putStringSet(KEY, singleton("/a"));
    verify(editor).apply();
  }

  public void testRemoveBookmarkRequestHandlerMethodIsConfigured() throws Exception {
    Method method = BookmarksProvider.class.getMethod("handle", RemoveBookmarkRequest.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testProducesBookmarksFromPreference() throws Exception {
    bookmark("/");
    assertEquals(new BookmarksSetting(new File("/")), handler.get());
  }

  public void testProducerMethodIsAnnotated() throws Exception {
    Method producer = BookmarksProvider.class.getMethod("get");
    assertNotNull(producer.getAnnotation(Produce.class));
  }

  @SuppressWarnings("unchecked")
  public void testReturnsDefaultBookmarksWhenNotSet() {
    given(pref.getStringSet(eq(KEY), anySet())).willAnswer(new Answer<Set<String>>() {
      @Override public Set<String> answer(InvocationOnMock invocation) {
        return (Set<String>) invocation.getArguments()[1];
      }
    });
    assertEquals(new BookmarksSetting(defaultFiles()), handler.get());
  }

  private Iterable<File> defaultFiles() {
    return transform(defaults, new Function<String, File>() {
      @Override public File apply(String input) {
        return new File(input);
      }
    });
  }

  public void testSkipsFilesThatDoNotExist() {
    bookmark("/", "/does-not-exits");
    assertEquals(new BookmarksSetting(new File("/")), handler.get());
  }

  @SuppressWarnings("unchecked")
  private void bookmark(String... bookmarks) {
    given(pref.getStringSet(eq(KEY), anySet())).willReturn(ImmutableSet.copyOf(bookmarks));
  }

  private SharedPreferences mockSharedPreferences(Editor editor) {
    SharedPreferences pref = mock(SharedPreferences.class);
    given(pref.edit()).willReturn(editor);
    return pref;
  }

  @SuppressWarnings("unchecked")
  private Editor mockEditor() {
    Editor editor = mock(Editor.class);
    given(editor.putStringSet(anyString(), anySet())).willReturn(editor);
    return editor;
  }
}
