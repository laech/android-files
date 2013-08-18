package l.files.app.mode;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE;
import static com.google.common.collect.Sets.newHashSet;
import static l.files.test.Mocks.mockMenuItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

import android.test.AndroidTestCase;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.squareup.otto.Bus;
import java.io.File;
import l.files.R;
import l.files.event.CopyRequest;
import org.mockito.ArgumentCaptor;

public final class CopyActionTest extends AndroidTestCase {

  private Menu menu;
  private MenuItem item;
  private ActionMode mode;
  private ListView list;
  private Bus bus;

  private CopyAction action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    item = mockMenuItem();
    menu = mockMenu(item);
    mode = mock(ActionMode.class);
    bus = mock(Bus.class);
    list = new ListView(getContext());
    list.setChoiceMode(CHOICE_MODE_MULTIPLE);
    action = new CopyAction(list, bus);
  }

  public void testCreatesMenuItemCorrectly() {
    action.onCreate(mode, menu);
    verify(item).setIcon(R.drawable.ic_menu_copy);
    verify(item).setOnMenuItemClickListener(action);
    verify(item).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
  }

  public void testFinishActionModeOnClick() {
    action.onCreate(mode, menu);
    action.onMenuItemClick(item);
    verify(mode).finish();
  }

  public void testCutsCheckedFiles() {
    list.setChoiceMode(CHOICE_MODE_MULTIPLE);
    list.setAdapter(new ArrayAdapter<File>(getContext(), 0, new File[]{
        new File("a"),
        new File("b"),
        new File("c")
    }));
    list.setItemChecked(0, true);
    list.setItemChecked(2, true);

    action.onMenuItemClick(item);

    CopyRequest request = captureRequest();
    assertEquals(newHashSet(new File("a"), new File("c")), request.value());
  }

  public void testCutsNothingIfNoCheckedFiles() {
    list.setChoiceMode(CHOICE_MODE_MULTIPLE);
    list.setAdapter(new ArrayAdapter<Object>(getContext(), 0, new String[]{"1"}));
    list.setItemChecked(0, true);

    action.onMenuItemClick(item);

    verify(bus, never()).post(anyObject());
  }

  private CopyRequest captureRequest() {
    ArgumentCaptor<CopyRequest> arg = ArgumentCaptor.forClass(CopyRequest.class);
    verify(bus).post(arg.capture());
    return arg.getValue();
  }

  private Menu mockMenu(MenuItem item) {
    Menu menu = mock(Menu.class);
    given(callAddMenuItem(menu)).willReturn(item);
    return menu;
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, android.R.id.copy, NONE, android.R.string.copy);
  }
}