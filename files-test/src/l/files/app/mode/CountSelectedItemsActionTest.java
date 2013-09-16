package l.files.app.mode;

import static android.widget.AbsListView.CHOICE_MODE_SINGLE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.view.ActionMode;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import l.files.R;
import l.files.test.BaseTest;

public final class CountSelectedItemsActionTest extends BaseTest {

  private CountSelectedItemsAction action;

  private ActionMode mode;
  private ListView list;

  @Override protected void setUp() throws Exception {
    super.setUp();
    list = new ListView(getContext());
    mode = mock(ActionMode.class);
    action = new CountSelectedItemsAction(list);
  }

  /**
   * Update on creation of action mode is necessary because if there are
   * existing items selected in action mode, then user rotates the screen, a
   * action mode will be created with existing items selected (and those items
   * won't be called with onItemCheckStateChanged in this case when the action
   * mode is created).
   */
  public void testUpdatesSelectedItemCountOnCreateActionMode() {
    testUpdatesTitle(new Runnable() {
      @Override public void run() {
        assertTrue(action.onCreateActionMode(mode, null));
      }
    });
  }

  public void testUpdatesSelectedItemCountOnItemCheckStateChange() {
    testUpdatesTitle(new Runnable() {
      @Override public void run() {
        action.onItemCheckedStateChanged(mode, 0, 0, true);
      }
    });
  }

  public void testUpdatesTitle(Runnable code) {
    list.setAdapter(new ArrayAdapter<Object>(getContext(), 0, new Object[]{"a"}));
    list.setChoiceMode(CHOICE_MODE_SINGLE);
    list.setItemChecked(0, true);

    code.run();

    verify(mode).setTitle(nSelectedString(1));
  }

  private String nSelectedString(int n) {
    return getContext().getResources().getString(R.string.n_selected, n);
  }
}
