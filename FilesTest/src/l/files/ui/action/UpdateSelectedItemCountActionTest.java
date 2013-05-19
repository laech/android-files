package l.files.ui.action;

import android.content.res.Resources;
import android.view.ActionMode;
import android.widget.ListView;
import junit.framework.TestCase;
import l.files.R;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class UpdateSelectedItemCountActionTest extends TestCase {

  private UpdateSelectedItemCountAction action;

  private ActionMode mode;
  private ListView listView;

  @Override protected void setUp() throws Exception {
    super.setUp();
    listView = mock(ListView.class);
    mode = mock(ActionMode.class);
    action = new UpdateSelectedItemCountAction(listView);
  }

  /**
   * Update on creation of action mode is necessary because if there are
   * existing items selected in action mode, then user rotates the screen, a
   * action mode will be created with existing items selected (and those items
   * won't be called with onItemCheckStateChanged in this case when the action
   * mode is created).
   */
  public void testUpdatesSelectedItemCountOnCreateActionMode() {
    String expected = setSelectedItemCount(11);
    assertTrue(action.onCreateActionMode(mode, null));
    verify(mode).setTitle(expected);
  }

  public void testUpdatesSelectedItemCountOnItemCheckStateChange() {
    String expected = setSelectedItemCount(100);
    action.onItemCheckedStateChanged(mode, 0, 0, true);
    verify(mode).setTitle(expected);
  }

  private String setSelectedItemCount(int count) {
    String template = "count: %s";
    String expected = String.format(template, count);
    Resources res = mock(Resources.class);
    given(res.getString(R.string.n_selected, count)).willReturn(expected);
    given(listView.getResources()).willReturn(res);
    given(listView.getCheckedItemCount()).willReturn(count);
    return expected;
  }
}
