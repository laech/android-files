package l.files.setting;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.ui.UserDirs.DIR_DCIM;
import static l.files.ui.UserDirs.DIR_DOWNLOADS;
import static l.files.ui.UserDirs.DIR_MOVIES;
import static l.files.ui.UserDirs.DIR_MUSIC;
import static l.files.ui.UserDirs.DIR_PICTURES;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

@SuppressWarnings("unchecked")
public final class BookmarksSettingTest extends TestCase {

  private SharedPreferences pref;
  private BookmarksSetting setting;

  @Override protected void setUp() throws Exception {
    super.setUp();
    pref = mockSharedPreferences();
    setting = new BookmarksSetting(pref);
  }

  public void testGet_returnsDefaultWhenNotSet() {
    assertThat(setting.get()).containsOnly(
        DIR_DCIM,
        DIR_DOWNLOADS,
        DIR_MOVIES,
        DIR_MUSIC,
        DIR_PICTURES);
  }

  public void testGet_skipsFilesThatDoNotExist() {
    setPaths("/", "/does-not-exits");
    assertThat(setting.get()).containsOnly(file("/"));
  }

  public void testSet_putsPathsToPref() {
    Editor editor = mockEditor();

    setting.set(newHashSet(file("/")));

    verify(editor).putStringSet(setting.key(), newHashSet("/"));
    verify(editor).apply();
  }

  public void testAdd() {
    Editor editor = mockEditor();
    setPaths("/");

    setting.add(file("/dev"));

    verify(editor).putStringSet(setting.key(), newHashSet("/", "/dev"));
    verify(editor).apply();
  }

  public void testRemove() {
    Editor editor = mockEditor();
    setPaths("/", "/dev");

    setting.remove(file("/"));

    verify(editor).putStringSet(setting.key(), newHashSet("/dev"));
    verify(editor).apply();
  }

  public void testContains_true() {
    setPaths("/");
    assertThat(setting.contains(file("/"))).isTrue();
  }

  public void testContains_false() {
    setPaths("/");
    assertThat(setting.contains(file("/dev"))).isFalse();
  }

  private Editor mockEditor() {
    Editor editor = mock(Editor.class);
    given(editor.putStringSet(eq(setting.key()), anySet())).willReturn(editor);
    given(pref.edit()).willReturn(editor);
    return editor;
  }

  private void setPaths(String... paths) {
    given(pref.getStringSet(eq(setting.key()), anySet()))
        .willReturn(newHashSet(paths));
  }

  private SharedPreferences mockSharedPreferences() {
    SharedPreferences pref = mock(SharedPreferences.class);
    given(pref.getStringSet(anyString(), anySet())).will(
        new Answer<Set<String>>() {
          @Override public Set<String> answer(InvocationOnMock invocation) {
            return (Set<String>) invocation.getArguments()[1];
          }
        });
    return pref;
  }

  private File file(String path) {
    return new File(path);
  }

}
