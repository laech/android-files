package l.files.app.mode;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.test.AndroidTestCase;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import l.files.R;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static l.files.app.mode.DeleteDialog.FRAGMENT_TAG;
import static l.files.test.Mocks.mockMenuItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class DeleteActionTest extends AndroidTestCase {

  private Menu menu;
  private MenuItem item;
  private ActionMode mode;
  private FragmentManager manager;

  private DeleteAction action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    item = mockMenuItem();
    menu = mockMenu(item);
    mode = mock(ActionMode.class);
    manager = mock(FragmentManager.class);
    action = new DeleteAction(new ListView(getContext()), manager);
  }

  public void testCreatesMenuItemCorrectly() {
    action.onCreate(mode, menu);
    verify(item).setIcon(R.drawable.ic_menu_delete);
    verify(item).setOnMenuItemClickListener(action);
    verify(item).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
  }

  public void testShowsConfirmDialogOnClick() {
    FragmentTransaction transaction = mock(FragmentTransaction.class);
    given(manager.beginTransaction()).willReturn(transaction);

    action.onMenuItemClick(item);

    verify(transaction).add(notNull(DeleteDialog.class), eq(FRAGMENT_TAG));
    verify(transaction).commit();
  }

  private Menu mockMenu(MenuItem item) {
    Menu menu = mock(Menu.class);
    given(callAddMenuItem(menu)).willReturn(item);
    return menu;
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, R.id.delete, NONE, R.string.delete);
  }
}
