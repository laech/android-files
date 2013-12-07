package l.files.common.app;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.view.Menu;
import l.files.test.BaseTest;

public final class BaseFragmentActivityTest extends BaseTest {

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
    verify(optionsMenu).onCreateOptionsMenu(menu);
  }

  public void testOnPrepareOptionsMenu_isDelegated() {
    activity.onPrepareOptionsMenu(menu);
    verify(optionsMenu).onPrepareOptionsMenu(menu);
  }

  public void testCallingMenuCallbacksWithoutSettingMenuWonNotCrash() {
    BaseFragmentActivity activity = new BaseFragmentActivity();
    activity.onCreateOptionsMenu(null);
    activity.onPrepareOptionsMenu(null);
    activity.onOptionsMenuClosed(null);
  }
}
