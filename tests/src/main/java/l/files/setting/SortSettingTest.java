package l.files.setting;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public final class SortSettingTest extends TestCase {

  private SharedPreferences pref;
  private SortSetting setting;

  @Override protected void setUp() throws Exception {
    super.setUp();
    pref = mockSharedPreferences();
    setting = new SortSetting(pref);
  }

  private SharedPreferences mockSharedPreferences() {
    SharedPreferences pref = mock(SharedPreferences.class);
    given(pref.getString(anyString(), anyString())).will(new Answer<String>() {
      @Override public String answer(InvocationOnMock invocation) {
        return (String) invocation.getArguments()[1];
      }
    });
    return pref;
  }

  public void testGet_returnsDefaultWhenNotSet() {
    assertThat(setting.get()).isEqualTo(getDefault());
  }

  public void testGet_returnsDefaultOnUnexpectedValue() {
    setPref("unknown-value");
    assertThat(setting.get()).isEqualTo(getDefault());
  }

  public void testGet_returnsSetValue() {
    setPref(SortBy.SIZE.name());
    assertThat(setting.get()).isEqualTo(SortBy.SIZE);
  }

  public void testSet_putsValueToPref() {
    Editor editor = mock(Editor.class);
    given(editor.putString(anyString(), anyString())).willReturn(editor);
    given(pref.edit()).willReturn(editor);

    setting.set(SortBy.DATE_MODIFIED);

    verify(editor).putString(setting.key(), SortBy.DATE_MODIFIED.name());
    verify(editor).apply();
  }

  private void setPref(String sort) {
    given(pref.getString(eq(setting.key()), anyString()))
        .willReturn(sort);
  }

  private SortBy getDefault() {
    return SortBy.NAME;
  }
}
