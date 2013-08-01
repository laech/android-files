package l.files.app.setting;

import android.content.SharedPreferences;
import com.google.common.base.Optional;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import junit.framework.TestCase;

import java.lang.reflect.Method;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public final class ViewOptionsProviderTest extends TestCase {

  private static final String KEY_SORT = "sort";
  private static final String KEY_HIDDEN_FILES = "show-hidden-files";

  private Bus bus;
  private SharedPreferences pref;
  private SharedPreferences.Editor editor;

  private ViewOptionsProvider handler;

  @Override protected void setUp() throws Exception {
    super.setUp();
    bus = mock(Bus.class);
    editor = mockEditor();
    pref = mockSharedPreferences(editor);
    handler = ViewOptionsProvider.register(bus, pref);
  }

  public void testListensOnSharedPreferences() {
    verify(pref).registerOnSharedPreferenceChangeListener(handler);
  }

  public void testSortRequestIsHandled() {
    handler.handle(new SortRequest("x"));
    verify(editor).putString(KEY_SORT, "x");
    verify(editor).apply();
  }

  public void testSortRequestHandlerMethodIsConfigured() throws Exception {
    Method method = ViewOptionsProvider.class.getMethod("handle", SortRequest.class);
    assertThat(method.getAnnotation(Subscribe.class)).isNotNull();
  }

  public void testShowHiddenFilesRequestIsHandled() {
    handler.handle(new ShowHiddenFilesRequest(true));
    verify(editor).putBoolean(KEY_HIDDEN_FILES, true);
    verify(editor).apply();
  }

  public void testShowHiddenFilesRequestHandlerMethodIsConfigured() throws Exception {
    Method method = ViewOptionsProvider.class.getMethod("handle", ShowHiddenFilesRequest.class);
    assertThat(method.getAnnotation(Subscribe.class)).isNotNull();
  }

  public void testNotifiesOnSortChange() {
    testNotify(KEY_SORT);
  }

  public void testNotifiesOnShowHiddenFilesChange() {
    testNotify(KEY_HIDDEN_FILES);
  }

  private void testNotify(String key) {
    given(pref.getString(KEY_SORT, null)).willReturn("y");
    given(pref.getBoolean(KEY_HIDDEN_FILES, false)).willReturn(true);
    handler.onSharedPreferenceChanged(pref, key);
    verify(bus).post(new ViewOptionsEvent(Optional.of("y"), true));
  }

  public void testNotifiesNothingIfSharePreferencesChangeIsUnrelated() {
    handler.onSharedPreferenceChanged(pref, "xyz");
    verify(bus, never()).post(any());
  }

  public void testProvidesViewEventAtInitialRegistration() throws Exception {
    given(pref.getString(KEY_SORT, null)).willReturn("z");
    given(pref.getBoolean(KEY_HIDDEN_FILES, false)).willReturn(true);
    assertThat(handler.get()).isEqualTo(new ViewOptionsEvent(Optional.of("z"), true));

    Method producer = ViewOptionsProvider.class.getMethod("get");
    assertThat(producer.getAnnotation(Produce.class)).isNotNull();
  }

  private SharedPreferences mockSharedPreferences(SharedPreferences.Editor editor) {
    SharedPreferences pref = mock(SharedPreferences.class);
    given(pref.edit()).willReturn(editor);
    return pref;
  }

  private SharedPreferences.Editor mockEditor() {
    SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);
    given(editor.putString(anyString(), anyString())).willReturn(editor);
    given(editor.putBoolean(anyString(), anyBoolean())).willReturn(editor);
    return editor;
  }
}
