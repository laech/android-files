package l.files.event;

import android.content.SharedPreferences;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import junit.framework.TestCase;

import java.lang.reflect.Method;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class SortProviderTest extends TestCase {

  private static final String KEY = "sort";

  private SharedPreferences pref;
  private SharedPreferences.Editor editor;
  private String defaultSort;

  private SortProvider handler;

  @Override protected void setUp() throws Exception {
    super.setUp();
    editor = mockEditor();
    pref = mockSharedPreferences(editor);
    defaultSort = "default";
    handler = new SortProvider(defaultSort);
    handler.register(mock(Bus.class), pref);
  }

  public void testSortRequestIsHandled() {
    handler.set(new SortRequest("x"));
    verify(editor).putString(KEY, "x");
    verify(editor).apply();
  }

  public void testSortRequestHandlerMethodIsConfigured() throws Exception {
    Method method = SortProvider.class.getMethod("set", SortRequest.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testProducesSettingFromPreference() throws Exception {
    given(pref.getString(KEY, defaultSort)).willReturn("z");
    assertEquals(new SortSetting("z"), handler.get());
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
    given(editor.putString(anyString(), anyString())).willReturn(editor);
    return editor;
  }
}
