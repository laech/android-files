package l.files;

import android.content.SharedPreferences;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import junit.framework.TestCase;
import l.files.event.SortRequest;
import l.files.event.ViewEvent;

import java.lang.reflect.Method;

import static l.files.event.Sort.DATE_MODIFIED;
import static l.files.event.Sort.NAME;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public final class ViewHandlerTest extends TestCase {

  private static final String KEY_SORT = "sort";
  private static final String KEY_HIDDEN_FILES = "show-hidden-files";

  private Bus bus;
  private SharedPreferences pref;
  private SharedPreferences.Editor editor;

  private ViewHandler handler;

  @Override protected void setUp() throws Exception {
    super.setUp();
    bus = mock(Bus.class);
    editor = mockEditor();
    pref = mockSharedPreferences(editor);
    handler = ViewHandler.register(bus, pref);
  }


  public void testListensOnSharedPreferences() {
    verify(pref).registerOnSharedPreferenceChangeListener(handler);
  }

  public void testSortRequestIsHandled() {
    handler.handle(new SortRequest(NAME));
    verify(editor).putString(KEY_SORT, NAME.name());
    verify(editor).apply();
  }

  public void testSortRequestHandlerMethodIsConfigured() throws Exception {
    Method method = ViewHandler.class.getMethod("handle", SortRequest.class);
    assertThat(method.getAnnotation(Subscribe.class)).isNotNull();
  }

  public void testNotifiesOnSortChange() {
    testNotify(KEY_SORT);
  }

  public void testNotifiesOnShowHiddenFilesChange() {
    testNotify(KEY_HIDDEN_FILES);
  }

  private void testNotify(String key) {
    given(pref.getString(KEY_SORT, NAME.name())).willReturn(DATE_MODIFIED.name());
    given(pref.getBoolean(KEY_HIDDEN_FILES, false)).willReturn(true);
    handler.onSharedPreferenceChanged(pref, key);
    verify(bus).post(new ViewEvent(DATE_MODIFIED, true));
  }

  public void testNotifiesNothingIfSharePreferencesChangeIsUnrelated() {
    handler.onSharedPreferenceChanged(pref, "xyz");
    verify(bus, never()).post(any());
  }

  public void testProvidesViewEventAtInitialRegistration() throws Exception {
    given(pref.getString(KEY_SORT, NAME.name())).willReturn(DATE_MODIFIED.name());
    given(pref.getBoolean(KEY_HIDDEN_FILES, false)).willReturn(true);
    assertThat(handler.get()).isEqualTo(new ViewEvent(DATE_MODIFIED, true));

    Method producer = ViewHandler.class.getMethod("get");
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
