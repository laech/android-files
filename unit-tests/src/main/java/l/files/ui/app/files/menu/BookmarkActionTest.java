package l.files.ui.app.files.menu;

import android.view.Menu;
import android.view.MenuItem;
import junit.framework.TestCase;
import l.files.R;
import l.files.setting.SetSetting;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.test.Mocks.mockMenuItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class BookmarkActionTest extends TestCase {

  private SetSetting<File> setting;
  private File dir;

  private BookmarkAction action;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    setting = mock(SetSetting.class);
    dir = mock(File.class);
    action = new BookmarkAction(dir, setting);
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

  public void testOnPrepare_unchecksMenuItemIfDirIsNotBookmarked() {
    testOnPrepare(false);
  }

  public void testOnPrepare_checksMenuItemIfFileIsBookmarked() {
    testOnPrepare(true);
  }

  public void testOnPrepare_doesNotCrashIfItemNotFound() {
    action.onPrepare(mock(Menu.class));
  }

  public void testOnMenuItemClick_bookmarksDirOnCheckingOfMenuItem() {
    testOnMenuItemClick(false, true);
  }

  public void testOnMenuItemClick_undoBookmarkingOfDirOnUncheckingOfMenuItem() {
    testOnMenuItemClick(true, false);
  }

  private void testOnPrepare(boolean favorite) {
    MenuItem item = mockMenuItem();
    Menu menu = mock(Menu.class);
    given(menu.findItem(R.id.bookmark)).willReturn(item);
    given(setting.contains(dir)).willReturn(favorite);

    action.onPrepare(menu);

    verify(item).setChecked(favorite);
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, R.id.bookmark, NONE, R.string.bookmark);
  }

  private void testOnMenuItemClick(boolean from, boolean to) {
    MenuItem item = mock(MenuItem.class);
    given(item.isChecked()).willReturn(from);
    assertTrue(action.onMenuItemClick(item));
    if (to) {
      verify(setting).add(dir);
    } else {
      verify(setting).remove(dir);
    }
  }

}
