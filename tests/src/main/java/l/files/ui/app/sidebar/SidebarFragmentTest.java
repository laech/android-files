package l.files.ui.app.sidebar;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.ListView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.event.BookmarksEvent;
import l.files.test.TestSidebarFragmentActivity;
import l.files.ui.event.FileSelectedEvent;

import java.io.File;
import java.lang.reflect.Method;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public final class SidebarFragmentTest
    extends ActivityInstrumentationTestCase2<TestSidebarFragmentActivity> {

  public SidebarFragmentTest() {
    super(TestSidebarFragmentActivity.class);
  }

  @UiThreadTest
  public void testBusIsRegisteredOnResume() {
    fragment().bus = mock(Bus.class);
    fragment().onResume();
    verify(fragment().bus).register(fragment());
  }

  @UiThreadTest
  public void testBusIsUnregisteredOnPause() {
    fragment().bus = mock(Bus.class);
    fragment().onResume();
    fragment().onPause();
    verify(fragment().bus).unregister(fragment());
  }

  @UiThreadTest
  public void testBusIsNotifiedOnFileSelection() throws Throwable {
    final File file = new File("/");
    fragment().bus = mock(Bus.class);

    adapter().clear();
    adapter().add(file);
    listView().performItemClick(null, 0, 0);

    verify(fragment().bus).post(new FileSelectedEvent(file));
  }

  @UiThreadTest
  public void testBusIsNotNotifiedOnNonFileSelection() throws Throwable {
    fragment().bus = mock(Bus.class);

    adapter().clear();
    adapter().add("hello");
    listView().performItemClick(null, 0, 0);

    verifyZeroInteractions(fragment().bus);
  }

  @UiThreadTest
  public void testHandlesBookmarkChanges() throws Throwable {
    final File file = new File("/");
    fragment().handle(new BookmarksEvent(file));
    assertAdapterContains(file);
  }

  public void testBookmarksChangedHandlerMethodIsAnnotated() throws Exception {
    Method method = SidebarFragment.class.getMethod("handle", BookmarksEvent.class);
    assertThat(method.getAnnotation(Subscribe.class)).isNotNull();
  }

  private void assertAdapterContains(File file) {
    for (int i = 0; i < adapter().getCount(); i++) {
      if (file.equals(adapter().getItem(i))) return;
    }
    fail();
  }

  private SidebarFragment fragment() {
    return getActivity().getFragment();
  }

  private ListView listView() {
    return fragment().getListView();
  }

  private SidebarAdapter adapter() {
    return fragment().getListAdapter();
  }

}
