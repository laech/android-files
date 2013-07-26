package l.files.ui.app;

import android.view.Menu;
import junit.framework.TestCase;
import l.files.ui.menu.OptionsMenu;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class BaseFragmentActivityTest extends TestCase {

  private Menu menu;
  private OptionsMenu optionsMenu;
  private BaseFragmentActivity activity;

  @Override protected void setUp() throws Exception {
    super.setUp();
    menu = mock(Menu.class);
    optionsMenu = mock(OptionsMenu.class);
    activity = new BaseFragmentActivity();
    activity.setOptionsMenu(optionsMenu);
  }

  public void testOnCreateOptionsMenu_isDelegated() {
    activity.onCreateOptionsMenu(menu);
    verify(optionsMenu).onCreate(menu);
  }

  public void testOnPrepareOptionsMenu_isDelegated() {
    activity.onPrepareOptionsMenu(menu);
    verify(optionsMenu).onPrepare(menu);
  }

  public void testOnOptionsMenuClosed_isDelegated() {
    activity.onOptionsMenuClosed(menu);
    verify(optionsMenu).onClose(menu);
  }

}
