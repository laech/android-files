package l.files.app.menu;

import android.view.Menu;
import android.view.MenuItem;
import com.google.common.base.Optional;
import com.squareup.otto.Bus;
import junit.framework.TestCase;
import l.files.R;
import l.files.setting.ShowHiddenFilesRequest;
import l.files.setting.ViewOptionsEvent;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.test.Mocks.mockMenuItem;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ShowHiddenFilesMenuTest extends TestCase {

  private Bus bus;
  private ShowHiddenFilesMenu action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    bus = mock(Bus.class);
    action = new ShowHiddenFilesMenu(bus);
  }

  public void testOnCreate() {
    MenuItem item = mockMenuItem();
    Menu menu = mock(Menu.class);
    given(callAddMenuItem(menu)).willReturn(item);

    action.onCreate(menu);

    callAddMenuItem(verify(menu));
    verify(item).setShowAsAction(SHOW_AS_ACTION_NEVER);
    verify(item).setCheckable(true);
    verify(item).setOnMenuItemClickListener(action);
  }

  public void testMenuItemIsUncheckedIfHideHiddenFiles() {
    testMenuItemChecked(false);
  }

  public void testMenuItemIsCheckedIfShowHiddenFiles() {
    testMenuItemChecked(true);
  }

  private void testMenuItemChecked(boolean showHiddenFiles) {
    MenuItem item = mockMenuItem();
    Menu menu = mock(Menu.class);
    given(menu.findItem(R.id.show_hidden_files)).willReturn(item);

    action.onPrepare(menu);
    if (showHiddenFiles) {
      action.handle(new ViewOptionsEvent(Optional.of("x"), true));
    } else {
      action.handle(new ViewOptionsEvent(Optional.of("x"), false));
    }

    verify(item).setChecked(showHiddenFiles);
  }

  public void testOnPrepareDoesNotCrashIfItemNotFound() {
    action.onPrepare(mock(Menu.class));
  }

  public void testShowsHiddenFileOnCheckingOfMenuItem() {
    testShowHiddenFiles(false, true);
  }

  public void testHidesHiddenFilesOnUncheckingOfMenuItem() {
    testShowHiddenFiles(true, false);
  }

  private void testShowHiddenFiles(boolean beforeState, boolean newState) {
    MenuItem item = mock(MenuItem.class);
    given(item.isChecked()).willReturn(beforeState);
    assertThat(action.onMenuItemClick(item)).isTrue();
    if (newState) {
      verify(bus).post(new ShowHiddenFilesRequest(newState));
    } else {
      verify(bus).post(new ShowHiddenFilesRequest(newState));
    }
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, R.id.show_hidden_files, NONE, R.string.show_hidden_files);
  }
}
