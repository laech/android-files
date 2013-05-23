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

public final class FavoriteActionTest extends TestCase {

  private Settings settings;
  private File file;

  private FavoriteAction action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    settings = mock(Settings.class);
    file = new File("a");
    action = new FavoriteAction(file, settings);
  }

  public void testGetItemIdReturnsIdOfFavorite() {
    assertEquals(R.id.favorite, action.getItemId());
  }

  public void testOnCreateOptionsMenuCreatesCheckableFavoriteMenuItem() {
    MenuItem item = mock(MenuItem.class);
    Menu menu = mock(Menu.class);
    given(callAddMenuItem(menu)).willReturn(item);

    action.onCreateOptionsMenu(menu);

    callAddMenuItem(verify(menu));
    verify(item).setShowAsAction(SHOW_AS_ACTION_NEVER);
    verify(item).setCheckable(true);
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, R.id.favorite, NONE, R.string.favorite);
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
    given(menu.findItem(R.id.favorite)).willReturn(item);
    given(settings.isFavorite(file)).willReturn(favorite);

    action.onPrepareOptionsMenu(menu);

    verify(item).setChecked(favorite);
  }

  public void testOnPrepareOptionsMenuDoesNotCrashIfItemNotFound() {
    action.onPrepareOptionsMenu(mock(Menu.class));
  }

  public void testOnOptionsItemSelectedWillAddFileToFavoriteOnCheckingOfMenuItem() {
    testOnOptionsItemSelected(false);
  }

  public void testOnOptionsItemSelectedWillRemoveFileFromFavoriteOnUncheckingOfMenuItem() {
    testOnOptionsItemSelected(true);
  }

  private void testOnOptionsItemSelected(boolean favorite) {
    MenuItem item = mock(MenuItem.class);
    given(item.isChecked()).willReturn(favorite);
    action.onOptionsItemSelected(item);
    verify(settings).setFavorite(file, !favorite);
  }

}
