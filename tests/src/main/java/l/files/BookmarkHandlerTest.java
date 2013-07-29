package l.files;

import android.content.SharedPreferences;
import com.google.common.collect.ImmutableSet;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import junit.framework.TestCase;
import l.files.event.AddBookmarkRequest;
import l.files.event.BookmarksEvent;
import l.files.event.RemoveBookmarkRequest;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Set;

import static android.content.SharedPreferences.Editor;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;
import static l.files.io.UserDirs.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.*;

public final class BookmarkHandlerTest extends TestCase {

  public static final String KEY = "bookmarks";

  private Bus bus;
  private SharedPreferences pref;
  private Editor editor;

  private BookmarkHandler handler;

  @Override protected void setUp() throws Exception {
    super.setUp();
    editor = mockEditor();
    pref = mockSharedPreferences(editor);
    bus = mock(Bus.class);
    handler = BookmarkHandler.register(bus, pref);
  }

  public void testListensOnSharedPreferences() {
    verify(pref).registerOnSharedPreferenceChangeListener(handler);
  }

  public void testAddBookmarkRequestIsHandled() {
    bookmark("/a");
    handler.handle(new AddBookmarkRequest(new File("/")));
    verify(editor).putStringSet(KEY, newHashSet("/", "/a"));
    verify(editor).apply();
  }

  public void testAddBookmarkRequestHandlerMethodIsConfigured() throws Exception {
    Method method = BookmarkHandler.class.getMethod("handle", AddBookmarkRequest.class);
    assertThat(method.getAnnotation(Subscribe.class)).isNotNull();
  }

  public void testRemoveBookmarkRequestIsHandled() {
    bookmark("/a", "/");
    handler.handle(new RemoveBookmarkRequest(new File("/")));
    verify(editor).putStringSet(KEY, singleton("/a"));
    verify(editor).apply();
  }

  public void testRemoveBookmarkRequestHandlerMethodIsConfigured() throws Exception {
    Method method = BookmarkHandler.class.getMethod("handle", RemoveBookmarkRequest.class);
    assertThat(method.getAnnotation(Subscribe.class)).isNotNull();
  }

  public void testNotifiesOnBookmarkChanges() {
    bookmark("/");
    handler.onSharedPreferenceChanged(pref, KEY);
    verify(bus).post(new BookmarksEvent(new File("/")));
  }

  public void testNotifiesNothingIfSharePreferencesChangeIsNotRelatedToBookmarks() {
    bookmark("/");
    handler.onSharedPreferenceChanged(pref, KEY + "xyz");
    verify(bus, never()).post(any());
  }

  public void testProvidesBookmarksAtInitialRegistration() throws Exception {
    bookmark("/");
    assertThat(handler.get()).isEqualTo(new BookmarksEvent(new File("/")));

    Method producer = BookmarkHandler.class.getMethod("get");
    assertThat(producer.getAnnotation(Produce.class)).isNotNull();
  }

  @SuppressWarnings("unchecked")
  public void testReturnsDefaultBookmarksWhenNotSet() {
    given(pref.getStringSet(eq(KEY), anySet())).willAnswer(new Answer<Set<String>>() {
      @Override public Set<String> answer(InvocationOnMock invocation) {
        return (Set<String>) invocation.getArguments()[1];
      }
    });
    assertThat(handler.get()).isEqualTo(new BookmarksEvent(
        DIR_DCIM,
        DIR_DOWNLOADS,
        DIR_MOVIES,
        DIR_MUSIC,
        DIR_PICTURES));
  }

  public void testSkipsFilesThatDoNotExist() {
    bookmark("/", "/does-not-exits");
    assertThat(handler.get()).isEqualTo(new BookmarksEvent(new File("/")));
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
