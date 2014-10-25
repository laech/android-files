package l.files.ui.menu;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.collect.Sets.newHashSet;
import static l.files.test.Mocks.mockMenuItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import android.view.Menu;
import android.view.MenuItem;
import com.squareup.otto.Bus;
import java.io.File;
import junit.framework.TestCase;
import l.files.event.Clipboard;
import l.files.event.PasteRequest;
import org.mockito.InOrder;

public final class PasteMenuTest extends TestCase {

  private Bus bus;
  private File dir;

  private PasteMenu action;

  @Override protected void setUp() throws Exception {
    super.setUp();
    bus = mock(Bus.class);
    dir = new File("/");
    action = new PasteMenu(bus, dir);
  }

  public void testOnCreate() {
    MenuItem item = mockMenuItem();
    Menu menu = mock(Menu.class);
    given(callAddMenuItem(menu)).willReturn(item);

    action.onCreate(menu);

    callAddMenuItem(verify(menu));
    verify(item).setShowAsAction(SHOW_AS_ACTION_NEVER);
    verify(item).setOnMenuItemClickListener(action);
  }

  public void testMenuItemIsUncheckedIfNoClipboard() {
    testMenuItemChecked(false);
  }

  public void testMenuItemIsCheckedIfHasClipboard() {
    testMenuItemChecked(true);
  }

  private void testMenuItemChecked(boolean hasClipboard) {
    MenuItem item = mockMenuItem();
    Menu menu = mock(Menu.class);
    given(menu.findItem(android.R.id.paste)).willReturn(item);

    action.onPrepare(menu);
    if (hasClipboard) {
      action.handle(new Clipboard.Cut(newHashSet(new File("/"))));
    }

    verify(item).setEnabled(hasClipboard);
  }

  public void testBusIsRegisteredAndUnregisteredOnPrepareToAvoidMemoryLeak() {
    action.onPrepare(mock(Menu.class));
    InOrder order = inOrder(bus);
    order.verify(bus).register(action);
    order.verify(bus).unregister(action);
    order.verifyNoMoreInteractions();
  }

  public void testOnPrepareDoesNotCrashIfItemNotFound() {
    action.onPrepare(mock(Menu.class));
  }

  public void testPostsPasteRequestOnCheckingOfMenuItem() {
    Clipboard clipboard = new Clipboard.Copy(newHashSet(new File("/")));
    action.handle(clipboard);
    action.onMenuItemClick(null);
    verify(bus).post(new PasteRequest.Copy(clipboard.value(), dir));
  }

  public void testPostsNothingIfNoClipboardOnCheckingOfMenuItem() {
    action.onMenuItemClick(null); // No crash
  }

  private MenuItem callAddMenuItem(Menu menu) {
    return menu.add(NONE, android.R.id.paste, NONE, android.R.string.paste);
  }
}
