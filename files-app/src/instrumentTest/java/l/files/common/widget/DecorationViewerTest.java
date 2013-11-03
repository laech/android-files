package l.files.common.widget;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.view.View;
import android.widget.FrameLayout;
import l.files.test.BaseTest;

public final class DecorationViewerTest extends BaseTest {

  private Viewer<Object> viewer;
  private Decorator<Object> decorator;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    decorator = mock(Decorator.class);
    viewer = Viewers.decorate(android.R.layout.simple_list_item_1, decorator);
  }

  public void testGetViewWillInflatesNewViewIfViewIsNull() {
    View view = viewer.getView(null, null, new FrameLayout(getContext()));
    assertNotNull(view);
    verify(decorator).decorate(view, null);
  }

  public void testGetViewWillReturnsViewIfViewIsNotNull() {
    Object item = new Object();
    View view = new View(getContext());
    View actual = viewer.getView(item, view, new FrameLayout(getContext()));
    assertSame(view, actual);
    verify(decorator).decorate(view, item);
  }
}
