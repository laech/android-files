package l.files.app;

import android.test.UiThreadTest;
import com.squareup.otto.Subscribe;
import l.files.setting.BookmarksSetting;
import l.files.test.TestSidebarFragmentActivity;

import java.io.File;
import java.lang.reflect.Method;

public final class SidebarFragmentTest
    extends BaseFileListFragmentTest<TestSidebarFragmentActivity> {

  public SidebarFragmentTest() {
    super(TestSidebarFragmentActivity.class);
  }

  @UiThreadTest public void testBookmarksChangeIsHandled() {
    final File file = new File("/");
    fragment().handle(new BookmarksSetting(file));
    assertAdapterContains(file);
  }

  public void testBookmarksChangedHandlerMethodIsAnnotated() throws Exception {
    Method method = SidebarFragment.class.getMethod("handle", BookmarksSetting.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
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
