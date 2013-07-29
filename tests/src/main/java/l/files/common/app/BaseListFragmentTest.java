package l.files.common.app;

import android.view.Menu;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class BaseListFragmentTest extends TestCase {

  private Menu menu;
  private OptionsMenu optionsMenu;
  private BaseListFragment fragment;

  @Override protected void setUp() throws Exception {
    super.setUp();
    menu = mock(Menu.class);
    optionsMenu = mock(OptionsMenu.class);
    fragment = new BaseListFragment();
    fragment.setOptionsMenu(optionsMenu);
  }

  public void testOnCreateOptionsMenu_isDelegated() {
    fragment.onCreateOptionsMenu(menu, null);
    verify(optionsMenu).onCreate(menu);
  }

  public void testOnPrepareOptionsMenu_isDelegated() {
    fragment.onPrepareOptionsMenu(menu);
    verify(optionsMenu).onPrepare(menu);
  }

  public void testOnOptionsMenuClosed_isDelegated() {
    fragment.onOptionsMenuClosed(menu);
    verify(optionsMenu).onClose(menu);
  }
}
