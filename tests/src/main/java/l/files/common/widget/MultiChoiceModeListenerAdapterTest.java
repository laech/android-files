package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;
import junit.framework.TestCase;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public final class MultiChoiceModeListenerAdapterTest extends TestCase {

  private MultiChoiceMode mode;
  private MultiChoiceModeListenerAdapter adapter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    mode = mock(MultiChoiceMode.class);
    adapter = new MultiChoiceModeListenerAdapter(mode);
  }

  public void testOnCreateActionMode_isDelegated() {
    ActionMode actionMode = mock(ActionMode.class);
    Menu menu = mock(Menu.class);
    assertThat(adapter.onCreateActionMode(actionMode, menu)).isTrue();
    verify(mode).onCreate(actionMode, menu);
  }

  public void testOnItemCheckedStateChanged_isDelegated() {
    ActionMode actionMode = mock(ActionMode.class);
    adapter.onItemCheckedStateChanged(actionMode, 0, 1, true);
    verify(mode).onChange(actionMode, 0, 1, true);
  }

  public void testOnPrepareActionMode_returnsFalseToIndicateNoNeedToUpdate() {
    assertThat(adapter.onPrepareActionMode(null, null)).isFalse();
    verifyZeroInteractions(mode);
  }
}
