package l.files.ui.app.sidebar;

import android.test.UiThreadTest;
import com.squareup.otto.Subscribe;
import l.files.event.BookmarksEvent;
import l.files.test.TestSidebarFragmentActivity;
import l.files.ui.app.BaseFileListFragmentTest;

import java.io.File;
import java.lang.reflect.Method;

import static org.fest.assertions.api.Assertions.assertThat;

public final class SidebarFragmentTest
    extends BaseFileListFragmentTest<TestSidebarFragmentActivity> {

  public SidebarFragmentTest() {
    super(TestSidebarFragmentActivity.class);
  }

  @UiThreadTest public void testBookmarksChangeIsHandled() {
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

  private SidebarAdapter adapter() {
    return fragment().getListAdapter();
  }

  @Override protected SidebarFragment fragment() {
    return getActivity().getFragment();
  }

}
