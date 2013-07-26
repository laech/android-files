package l.files.ui.menu;

import android.view.Menu;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class CompositeOptionsMenuTest extends TestCase {

  private OptionsMenu action1;
  private OptionsMenu action2;

  private CompositeOptionsMenu optionsMenu;

  @Override protected void setUp() throws Exception {
    super.setUp();
    action1 = mock(OptionsMenu.class);
    action2 = mock(OptionsMenu.class);
    optionsMenu = new CompositeOptionsMenu(action1, action2);
  }

  public void testOnCreate_isDelegated() {
    Menu menu = mock(Menu.class);
    optionsMenu.onCreate(menu);
    verify(action1).onCreate(menu);
    verify(action2).onCreate(menu);
  }

  public void testOnPrepare_isDelegated() {
    Menu menu = mock(Menu.class);
    optionsMenu.onPrepare(menu);
    verify(action1).onPrepare(menu);
    verify(action2).onPrepare(menu);
  }

  public void testOnClose_isDelegated() {
    Menu menu = mock(Menu.class);
    optionsMenu.onClose(menu);
    verify(action1).onClose(menu);
    verify(action2).onClose(menu);
  }

}
