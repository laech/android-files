package l.files.ui.menu;

import android.view.Menu;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class OptionsMenuTest extends TestCase {

  private OptionsMenuAction action1;
  private OptionsMenuAction action2;

  private OptionsMenu optionsMenu;

  @Override protected void setUp() throws Exception {
    super.setUp();
    action1 = mock(OptionsMenuAction.class);
    action2 = mock(OptionsMenuAction.class);
    optionsMenu = new OptionsMenu(action1, action2);
  }

  public void testOnCreateOptionsMenuIsDelegated() {
    Menu menu = mock(Menu.class);
    optionsMenu.onCreateOptionsMenu(menu);
    verify(action1).onCreate(menu);
    verify(action2).onCreate(menu);
  }

  public void testOnPrepareOptionsMenuIsDelegated() {
    Menu menu = mock(Menu.class);
    optionsMenu.onPrepareOptionsMenu(menu);
    verify(action1).onPrepare(menu);
    verify(action2).onPrepare(menu);
  }

}
