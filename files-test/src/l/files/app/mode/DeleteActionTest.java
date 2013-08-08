package l.files.app.mode;

import android.content.Context;
import android.test.AndroidTestCase;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import l.files.R;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static l.files.test.Mocks.mockMenuItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class DeleteActionTest extends AndroidTestCase {

  private Menu menu;
  private MenuItem item;
  private ActionMode mode;
  private AbsListView list;
  private Context context;
  private DeleteAction action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    item = mockMenuItem();
    menu = mockMenu(item);
    mode = mock(ActionMode.class);
    list = new ListView(getContext());
    context = mock(Context.class);
    action = new DeleteAction(context, list);
  }

  public void testCreatesMenuItemCorrectly() {
    action.onCreate(mode, menu);
    verify(item).setIcon(android.R.drawable.ic_menu_delete);
    verify(item).setOnMenuItemClickListener(action);
    verify(item).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
  }

  public void testFinishesModeOnClick() {
    action.onCreate(mode, mockMenu(item));
    action.onMenuItemClick(item);
    verify(mode).finish();
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
