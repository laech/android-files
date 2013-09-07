package l.files.common.widget;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.view.ActionMode;
import android.view.Menu;
import l.files.test.BaseTest;

public final class CompositeMultiChoiceActionTest extends BaseTest {

  private MultiChoiceAction action1;
  private MultiChoiceAction action2;
  private CompositeMultiChoiceAction composite;

  @Override protected void setUp() throws Exception {
    super.setUp();
    action1 = mock(MultiChoiceAction.class);
    action2 = mock(MultiChoiceAction.class);
    composite = new CompositeMultiChoiceAction(action1, action2);
  }

  public void testOnCreateIsDelegated() {
    ActionMode mode = mock(ActionMode.class);
    Menu menu = mock(Menu.class);
    composite.onCreate(mode, menu);
    verify(action1).onCreate(mode, menu);
    verify(action2).onCreate(mode, menu);
  }

  public void testOnChangeIsDelegated() {
    ActionMode mode = mock(ActionMode.class);
    composite.onChange(mode, 0, 1, true);
    verify(action1).onChange(mode, 0, 1, true);
    verify(action2).onChange(mode, 0, 1, true);
  }

  public void testOnDestroyIsDelegated() {
    ActionMode mode = mock(ActionMode.class);
    composite.onDestroy(mode);
    verify(action1).onDestroy(mode);
    verify(action2).onDestroy(mode);
  }
}
