package l.files.ui.app.files;

import android.test.AndroidTestCase;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import l.files.R;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE;
import static l.files.trash.TrashService.TrashMover;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class MoveToTrashActionTest extends AndroidTestCase {

  private ListView list;
  private TrashMover mover;
  private ActionMode mode;

  @Override protected void setUp() throws Exception {
    super.setUp();
    mode = mock(ActionMode.class);
    mover = mock(TrashMover.class);
    list = new ListView(getContext());
  }

  public void testItemIdIsMoveToTrash() {
    MoveToTrashAction action = create(list, mover);
    assertEquals(R.id.move_to_trash, action.getItemId());
  }

  public void testCreatesActionItem() {
    MenuItem item = mock(MenuItem.class);
    Menu menu = mock(Menu.class);
    given(callAddMenuItem(menu)).willReturn(item);

    assertTrue(create(list, mover).onCreateActionMode(null, menu));

    callAddMenuItem(verify(menu));
    verify(item).setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, R.id.move_to_trash, NONE, R.string.move_to_trash);
  }

  public void testMovesFilesToTrashOnClick() {
    list.setAdapter(newAdapter(
        new File("0"),
        "1",
        new File("2"),
        new File("3")
    ));
    list.setChoiceMode(CHOICE_MODE_MULTIPLE);
    list.setItemChecked(0, true);
    list.setItemChecked(1, true);
    list.setItemChecked(2, true);
    list.setItemChecked(3, false);

    MoveToTrashAction action = create(list, mover);
    assertTrue(action.onActionItemClicked(mode, null));

    verify(mover).moveToTrash(new File("0"));
    verify(mover).moveToTrash(new File("2"));
  }

  public void testFinishesActionModeOnClick() {
    MoveToTrashAction action = create(list, mover);
    action.onActionItemClicked(mode, null);
    verify(mode).finish();
  }

  private ArrayAdapter<Object> newAdapter(Object... items) {
    return new ArrayAdapter<Object>(
        getContext(), android.R.layout.simple_list_item_1, items);
  }

  private MoveToTrashAction create(AbsListView list, TrashMover mover) {
    return new MoveToTrashAction(list, mover);
  }
}
