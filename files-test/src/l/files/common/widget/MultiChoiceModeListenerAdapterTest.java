package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;
import junit.framework.TestCase;

import static org.mockito.Mockito.*;

public final class MultiChoiceModeListenerAdapterTest extends TestCase {

  private MultiChoiceAction mode;
  private MultiChoiceModeListenerAdapter adapter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    mode = mock(MultiChoiceAction.class);
    adapter = new MultiChoiceModeListenerAdapter(mode);
  }

  public void testOnCreateActionModeIsDelegated() {
    ActionMode actionMode = mock(ActionMode.class);
    Menu menu = mock(Menu.class);
    assertTrue(adapter.onCreateActionMode(actionMode, menu));
    verify(mode).onCreate(actionMode, menu);
  }

  public void testOnItemCheckedStateChangedIsDelegated() {
    ActionMode actionMode = mock(ActionMode.class);
    adapter.onItemCheckedStateChanged(actionMode, 0, 1, true);
    verify(mode).onChange(actionMode, 0, 1, true);
  }

  public void testOnPrepareActionModeReturnsFalseToIndicateNoNeedToUpdate() {
    assertFalse(adapter.onPrepareActionMode(null, null));
    verifyZeroInteractions(mode);
  }

  public void testOnDestroyActionModeIsDelegated() {
    ActionMode actionMode = mock(ActionMode.class);
    adapter.onDestroyActionMode(actionMode);
    verify(mode).onDestroy(actionMode);
  }
}
