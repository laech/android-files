package l.files.app.mode;

import static android.content.ClipDescription.MIMETYPE_TEXT_INTENT;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE;
import static l.files.app.Intents.ACTION_CUT;
import static l.files.test.Mocks.mockMenuItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.File;
import l.files.R;
import org.mockito.ArgumentCaptor;

public final class CutActionTest extends AndroidTestCase {

  private Menu menu;
  private MenuItem item;
  private ActionMode mode;
  private ListView list;
  private ClipboardManager manager;

  private CutAction action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    item = mockMenuItem();
    menu = mockMenu(item);
    mode = mock(ActionMode.class);
    manager = mock(ClipboardManager.class);
    list = new ListView(getContext());
    action = new CutAction(list, manager);
  }

  public void testCreatesMenuItemCorrectly() {
    action.onCreate(mode, menu);
    verify(item).setIcon(R.drawable.ic_menu_cut);
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

    ClipData clip = captureClipData();
    assertDescription(clip.getDescription());
    assertIntent(clip.getItemAt(0).getIntent(), new File("a"));
    assertIntent(clip.getItemAt(1).getIntent(), new File("c"));
    assertEquals(2, clip.getItemCount());
  }

  private static void assertIntent(Intent intent, File file) {
    assertEquals(ACTION_CUT, intent.getAction());
    assertEquals(Uri.fromFile(file), intent.getData());
  }

  private static void assertDescription(ClipDescription description) {
    assertEquals(1, description.getMimeTypeCount());
    assertEquals(MIMETYPE_TEXT_INTENT, description.getMimeType(0));
  }

  private ClipData captureClipData() {
    ArgumentCaptor<ClipData> arg = ArgumentCaptor.forClass(ClipData.class);
    verify(manager).setPrimaryClip(arg.capture());
    return arg.getValue();
  }

  private Menu mockMenu(MenuItem item) {
    Menu menu = mock(Menu.class);
    given(callAddMenuItem(menu)).willReturn(item);
    return menu;
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, android.R.id.cut, NONE, android.R.string.cut);
  }
}
