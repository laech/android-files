package l.files.ui.app.files.mode;

import android.content.Context;
import android.content.res.Resources;
import android.test.AndroidTestCase;
import android.view.ActionMode;
import android.widget.ListView;
import l.files.R;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class UpdateSelectedItemCountActionTest extends AndroidTestCase {

  private UpdateSelectedItemCountAction action;

  private ActionMode mode;
  private MockListView list;

  @Override protected void setUp() throws Exception {
    super.setUp();
    list = new MockListView(getContext());
    mode = mock(ActionMode.class);
    action = new UpdateSelectedItemCountAction(list);
  }

  /**
   * Update on creation of action mode is necessary because if there are
   * existing items selected in action mode, then user rotates the screen, a
   * action mode will be created with existing items selected (and those items
   * won't be called with onItemCheckStateChanged in this case when the action
   * mode is created).
   */
  public void testUpdatesSelectedItemCountOnCreateActionMode() {
    String expected = list.setCheckedItemCount(11);
    assertTrue(action.onCreateActionMode(mode, null));
    verify(mode).setTitle(expected);
  }

  public void testUpdatesSelectedItemCountOnItemCheckStateChange() {
    String expected = list.setCheckedItemCount(100);
    action.onItemCheckedStateChanged(mode, 0, 0, true);
    verify(mode).setTitle(expected);
  }

  // https://code.google.com/p/dexmaker/issues/detail?id=9
  private static class MockListView extends ListView {

    private final Resources res = mock(Resources.class);
    private int checkedItemCount = -1;

    MockListView(Context context) {
      super(context);
    }

    @Override public int getCheckedItemCount() {
      return checkedItemCount;
    }

    @Override public Resources getResources() {
      return res;
    }

    String setCheckedItemCount(int count) {
      checkedItemCount = count;
      String template = "count: %s";
      String expected = String.format(template, count);
      given(res.getString(R.string.n_selected, count)).willReturn(expected);
      return expected;
    }
  }
}
