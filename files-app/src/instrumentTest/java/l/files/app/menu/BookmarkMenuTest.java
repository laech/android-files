package l.files.ui.menu;

import android.view.Menu;
import android.view.MenuItem;
import com.squareup.otto.Bus;
import junit.framework.TestCase;
import l.files.R;
import l.files.event.AddBookmarkRequest;
import l.files.event.BookmarksSetting;
import l.files.event.RemoveBookmarkRequest;
import org.mockito.InOrder;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.test.Mocks.mockMenuItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public final class BookmarkMenuTest extends TestCase {

  private Bus bus;
  private File dir;

  private BookmarkMenu action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    bus = mock(Bus.class);
    dir = new File("/");
    action = new BookmarkMenu(bus, bookmarks, dir);
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

  public void testMenuItemIsUncheckedIfDirIsNotBookmarked() {
    testMenuItemChecked(false);
  }

  public void testMenuItemIsCheckedIfFileIsBookmarked() {
    testMenuItemChecked(true);
  }

  private void testMenuItemChecked(boolean favorite) {
    MenuItem item = mockMenuItem();
    Menu menu = mock(Menu.class);
    given(menu.findItem(R.id.bookmark)).willReturn(item);

    action.onPrepare(menu);
    if (favorite) {
      action.handle(new BookmarksSetting(dir));
    } else {
      action.handle(new BookmarksSetting(new File("/xyz")));
    }

    verify(item).setChecked(favorite);
  }

  public void testBusIsRegisteredAndUnregisteredOnPrepareToAvoidMemoryLeak() {
    action.onPrepare(null);
    InOrder order = inOrder(bus);
    order.verify(bus).register(action);
    order.verify(bus).unregister(action);
    order.verifyNoMoreInteractions();
  }

  public void testOnPrepareDoesNotCrashIfItemNotFound() {
    action.onPrepare(mock(Menu.class));
  }

  public void testBookmarkIsAddedOnCheckingOfMenuItem() {
    testBookmark(false, true);
  }

  public void testBookmarkIsRemovedOnUncheckingOfMenuItem() {
    testBookmark(true, false);
  }

  private void testBookmark(boolean from, boolean to) {
    MenuItem item = mock(MenuItem.class);
    given(item.isChecked()).willReturn(from);
    assertTrue(action.onMenuItemClick(item));
    if (to) {
      verify(bus).post(new AddBookmarkRequest(dir));
    } else {
      verify(bus).post(new RemoveBookmarkRequest(dir));
    }
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, R.id.bookmark, NONE, R.string.bookmark);
  }
}
