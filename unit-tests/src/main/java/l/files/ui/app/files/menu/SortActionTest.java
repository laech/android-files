package l.files.ui.app.files.menu;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import com.google.common.base.Supplier;
import junit.framework.TestCase;
import l.files.R;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.test.Mocks.mockMenuItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class SortActionTest extends TestCase {

  private FragmentManager manager;
  private DialogFragment dialog;

  private SortAction action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    manager = mock(FragmentManager.class);
    dialog = mock(DialogFragment.class);
    action = new SortAction(manager, new Supplier<DialogFragment>() {
      @Override public DialogFragment get() {
        return dialog;
      }
    });
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
    action.onMenuItemClick(null);
    verify(dialog).show(manager, null);
  }

  private MenuItem callAddMenuItemMethod(Menu menu) {
    return menu.add(NONE, R.id.sort_by, NONE, R.string.sort_by);
  }
}
