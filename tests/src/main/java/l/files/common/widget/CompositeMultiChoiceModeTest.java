package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class CompositeMultiChoiceModeTest extends TestCase {

  private CompositeMultiChoiceMode composite;

  private MultiChoiceMode action1;
  private MultiChoiceMode action2;

  @Override protected void setUp() throws Exception {
    super.setUp();
    action1 = mock(MultiChoiceMode.class);
    action2 = mock(MultiChoiceMode.class);
    composite = new CompositeMultiChoiceMode(action1, action2);
  }

  public void testOnCreate_isDelegated() {
    ActionMode mode = mock(ActionMode.class);
    Menu menu = mock(Menu.class);
    composite.onCreate(mode, menu);
    verify(action1).onCreate(mode, menu);
    verify(action2).onCreate(mode, menu);
  }

  public void testOnChange_isDelegated() {
    ActionMode mode = mock(ActionMode.class);
    composite.onChange(mode, 0, 1, true);
    verify(action1).onChange(mode, 0, 1, true);
    verify(action2).onChange(mode, 0, 1, true);
  }
}
