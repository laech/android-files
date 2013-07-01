package l.files.setting;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public final class ShowHiddenFilesSettingTest extends TestCase {

  private SharedPreferences pref;
  private ShowHiddenFilesSetting setting;

  @Override protected void setUp() throws Exception {
    super.setUp();
    pref = mockSharedPreferences();
    setting = new ShowHiddenFilesSetting(pref);
  }

  public void testGet_returnsDefaultValueAsDoNotShowHiddenFiles() {
    assertThat(setting.get()).isFalse();
  }

  public void testGet_returnsSetValue() {
    setValue(true);
    assertThat(setting.get()).isTrue();
  }

  public void testSet_putsValueToPref() {
    Editor editor = mockEditor();
    setting.set(true);
    verify(editor).putBoolean(setting.key(), true);
    verify(editor).apply();
  }

  private Editor mockEditor() {
    Editor editor = mock(Editor.class);
    given(editor.putBoolean(eq(setting.key()), anyBoolean()))
        .willReturn(editor);
    given(pref.edit()).willReturn(editor);
    return editor;
  }

  private void setValue(boolean value) {
    given(pref.getBoolean(eq(setting.key()), anyBoolean())).willReturn(value);
  }

  private SharedPreferences mockSharedPreferences() {
    SharedPreferences pref = mock(SharedPreferences.class);
    given(pref.getBoolean(anyString(), anyBoolean())).will(
        new Answer<Boolean>() {
          @Override public Boolean answer(InvocationOnMock invocation) {
            return (Boolean) invocation.getArguments()[1];
          }
        });
    return pref;
  }
}
