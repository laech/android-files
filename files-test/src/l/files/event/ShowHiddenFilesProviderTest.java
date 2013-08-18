package l.files.event;

import android.content.SharedPreferences;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import junit.framework.TestCase;

import java.lang.reflect.Method;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ShowHiddenFilesProviderTest extends TestCase {

  private static final String KEY = "show-hidden-files";

  private SharedPreferences pref;
  private SharedPreferences.Editor editor;
  private boolean showByDefault;

  private ShowHiddenFilesProvider handler;

  @Override protected void setUp() throws Exception {
    super.setUp();
    editor = mockEditor();
    pref = mockSharedPreferences(editor);
    showByDefault = false;
    handler = new ShowHiddenFilesProvider(showByDefault);
    handler.register(mock(Bus.class), pref);
  }

  public void testShowHiddenFilesRequestIsHandled() {
    handler.set(new ShowHiddenFilesRequest(false));
    verify(editor).putBoolean(KEY, false);
    verify(editor).apply();
  }

  public void testShowHiddenFilesRequestHandlerMethodIsConfigured() throws Exception {
    Method method = ShowHiddenFilesProvider.class.getMethod("set", ShowHiddenFilesRequest.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testProducesSettingFromPreference() throws Exception {
    given(pref.getBoolean(KEY, showByDefault)).willReturn(true);
    assertEquals(new ShowHiddenFilesSetting(true), handler.get());
  }

  public void testProducerMethodIsAnnotated() throws Exception {
    Method producer = SortProvider.class.getMethod("get");
    assertNotNull(producer.getAnnotation(Produce.class));
  }

  private SharedPreferences mockSharedPreferences(SharedPreferences.Editor editor) {
    SharedPreferences pref = mock(SharedPreferences.class);
    given(pref.edit()).willReturn(editor);
    return pref;
  }

  private SharedPreferences.Editor mockEditor() {
    SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);
    given(editor.putBoolean(anyString(), anyBoolean())).willReturn(editor);
    return editor;
  }
}
