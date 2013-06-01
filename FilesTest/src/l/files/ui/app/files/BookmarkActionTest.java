package l.files.ui.app.files;

import android.view.Menu;
import android.view.MenuItem;
import junit.framework.TestCase;
import l.files.R;
import l.files.Settings;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class BookmarkActionTest extends TestCase {

  private Settings settings;
  private File file;

  private BookmarkAction action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    settings = mock(Settings.class);
    file = new File("a");
    action = new BookmarkAction(file, settings);
  }

  public void testOnCreateOptionsMenuCreatesCheckableFavoriteMenuItem() {
    MenuItem item = mock(MenuItem.class);
    Menu menu = mock(Menu.class);
    given(callAddMenuItem(menu)).willReturn(item);

    action.onCreate(menu);

    callAddMenuItem(verify(menu));
    verify(item).setShowAsAction(SHOW_AS_ACTION_NEVER);
    verify(item).setCheckable(true);
    verify(item).setOnMenuItemClickListener(action);
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, R.id.bookmark, NONE, R.string.bookmark);
  }

  public void testOnPrepareOptionsMenuUnchecksMenuItemIfFileIsAFavorite() {
    testOnPrepareOptionsMenu(false);
  }

  public void testOnPrepareOptionsMenuChecksMenuItemIfFileIsNotAFavorite() {
    testOnPrepareOptionsMenu(true);
  }

  private void testOnPrepareOptionsMenu(boolean favorite) {
    MenuItem item = mock(MenuItem.class);
    Menu menu = mock(Menu.class);
    given(menu.findItem(R.id.bookmark)).willReturn(item);
    given(settings.isBookmark(file)).willReturn(favorite);

    action.onPrepare(menu);

    verify(item).setChecked(favorite);
  }

  public void testOnPrepareOptionsMenuDoesNotCrashIfItemNotFound() {
    action.onPrepare(mock(Menu.class));
  }

  public void testOnMenuItemClickWillAddFileToFavoriteOnCheckingOfMenuItem() {
    testOnMenuItemClick(false);
  }

  public void testOnMenuItemClickWillRemoveFileFromFavoriteOnUncheckingOfMenuItem() {
    testOnMenuItemClick(true);
  }

  private void testOnMenuItemClick(boolean favorite) {
    MenuItem item = mock(MenuItem.class);
    given(item.isChecked()).willReturn(favorite);
    assertTrue(action.onMenuItemClick(item));
    verify(settings).setFavorite(file, !favorite);
  }

}
