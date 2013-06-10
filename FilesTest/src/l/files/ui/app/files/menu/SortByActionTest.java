package l.files.ui.app.files.menu;

import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import com.google.common.base.Supplier;
import junit.framework.TestCase;
import l.files.R;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class SortByActionTest extends TestCase {

  private SortByAction action;
  private SortByDialog dialog;
  private FragmentManager manager;

  @Override protected void setUp() throws Exception {
    super.setUp();
    manager = mock(FragmentManager.class);
    dialog = mock(SortByDialog.class);
    action = new SortByAction(manager, new Supplier<SortByDialog>() {
      @Override public SortByDialog get() {
        return dialog;
      }
    });
  }

  public void testCreatesMenuItem() {
    MenuItem item = mock(MenuItem.class);
    Menu menu = mock(Menu.class);
    given(callAddMenuItemMethod(menu)).willReturn(item);

    action.onCreate(menu);

    callAddMenuItemMethod(verify(menu));
    verify(item).setShowAsAction(SHOW_AS_ACTION_NEVER);
    verify(item).setOnMenuItemClickListener(action);
  }

  private MenuItem callAddMenuItemMethod(Menu menu) {
    return menu.add(NONE, R.id.sort_by, NONE, R.string.sort_by);
  }

  public void testShowsDialogOnClick() {
    action.onMenuItemClick(null);
    verify(dialog).show(manager, null);
  }
}
