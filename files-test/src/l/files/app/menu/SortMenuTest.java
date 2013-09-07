package l.files.app.menu;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.test.Mocks.mockMenuItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import l.files.R;
import l.files.test.BaseTest;

public final class SortMenuTest extends BaseTest {

  private FragmentManager manager;

  private SortMenu action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    manager = mock(FragmentManager.class);
    action = new SortMenu(manager);
  }

  public void testOnCreate() {
    MenuItem item = mockMenuItem();
    Menu menu = mock(Menu.class);
    given(callAddMenuItemMethod(menu)).willReturn(item);

    action.onCreate(menu);

    callAddMenuItemMethod(verify(menu));
    verify(item).setShowAsAction(SHOW_AS_ACTION_NEVER);
    verify(item).setOnMenuItemClickListener(action);
  }

  public void testOnMenuItemClick_showsDialog() {
    FragmentTransaction transaction = mock(FragmentTransaction.class);
    given(manager.beginTransaction()).willReturn(transaction);

    action.onMenuItemClick(null);

    verify(transaction).add(notNull(SortDialog.class), eq(SortDialog.FRAGMENT_TAG));
    verify(transaction).commit();
  }

  private MenuItem callAddMenuItemMethod(Menu menu) {
    return menu.add(NONE, R.id.sort_by, NONE, R.string.sort_by);
  }
}
