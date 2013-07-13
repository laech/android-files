package l.files.ui.widget;

import android.R;
import android.test.AndroidTestCase;
import android.view.View;
import android.widget.FrameLayout;

import static org.fest.assertions.api.Assertions.assertThat;

public final class LayoutViewerTest extends AndroidTestCase {

  private int layoutId;
  private LayoutViewer<Object> viewer;

  @Override protected void setUp() throws Exception {
    super.setUp();
    layoutId = R.layout.simple_list_item_1;
    viewer = new LayoutViewer<Object>(layoutId);
  }

  public void testGetView_inflatesNewViewIfViewIsNull() {
    View view = viewer.getView(null, null, new FrameLayout(getContext()));
    assertThat(view).isNotNull();
  }

  public void testGetView_returnsViewIfViewIsNotNull() {
    View expected = new View(getContext());
    View actual = viewer.getView(null, expected, new FrameLayout(getContext()));
    assertThat(actual).isSameAs(expected);
  }
}
