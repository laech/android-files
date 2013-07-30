package l.files.common.widget;

import android.test.AndroidTestCase;
import android.view.View;
import android.widget.FrameLayout;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class DecorationViewerTest extends AndroidTestCase {

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
    assertThat(view).isNotNull();
    verify(decorator).decorate(view, null);
  }

  public void testGetViewWillReturnsViewIfViewIsNotNull() {
    Object item = new Object();
    View view = new View(getContext());
    View actual = viewer.getView(item, view, new FrameLayout(getContext()));
    assertThat(actual).isSameAs(view);
    verify(decorator).decorate(view, item);
  }
}
