package l.files.ui.menu;

import android.view.Menu;
import android.view.MenuItem;
import junit.framework.TestCase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
    verify(action1).onCreateOptionsMenu(menu);
    verify(action2).onCreateOptionsMenu(menu);
  }

  public void testOnPrepareOptionsMenuIsDelegated() {
    Menu menu = mock(Menu.class);
    optionsMenu.onPrepareOptionsMenu(menu);
    verify(action1).onPrepareOptionsMenu(menu);
    verify(action2).onPrepareOptionsMenu(menu);
  }

  public void testOnOptionsItemSelectedIsDelegatedToActionWithSameItemId() {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(1);
    given(action1.getItemId()).willReturn(1);

    assertTrue(optionsMenu.onOptionsItemSelected(item));

    verify(action1).onOptionsItemSelected(item);
    verify(action2, never()).onOptionsItemSelected(item);
  }

  public void testOnOptionsItemSelectedIsNotDelegatedIfActionHasNoItemIdAndMenuItemHasNoId() {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(0);
    given(action1.getItemId()).willReturn(0);
    given(action2.getItemId()).willReturn(1);

    assertFalse(optionsMenu.onOptionsItemSelected(item));

    verify(action1, never()).onOptionsItemSelected(item);
    verify(action2, never()).onOptionsItemSelected(item);
  }

}
