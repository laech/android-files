package l.files.app.mode;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static l.files.test.Mocks.mockMenuItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import l.files.R;
import l.files.test.BaseTest;

public final class SelectAllActionTest extends BaseTest {

  private Menu menu;
  private MenuItem item;
  private ActionMode mode;

  private SelectAllAction action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    item = mockMenuItem();
    menu = mockMenu(item);
    mode = mock(ActionMode.class);
    action = new SelectAllAction(new ListView(getContext()));
  }

  public void testCreatesMenuItemCorrectly() {
    action.onCreate(mode, menu);
    verify(item).setIcon(R.drawable.ic_menu_select_all);
    verify(item).setOnMenuItemClickListener(action);
    verify(item).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
  }

  private Menu mockMenu(MenuItem item) {
    Menu menu = mock(Menu.class);
    given(callAddMenuItem(menu)).willReturn(item);
    return menu;
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, android.R.id.selectAll, NONE, android.R.string.selectAll);
  }
}
