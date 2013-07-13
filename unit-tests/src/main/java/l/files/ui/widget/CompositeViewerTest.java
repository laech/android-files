package l.files.ui.widget;

import android.view.View;
import junit.framework.TestCase;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class CompositeViewerTest extends TestCase {

  private Viewer<Object> viewer1;
  private Viewer<Object> viewer2;

  private CompositeViewer<Object> composite;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    viewer1 = mock(Viewer.class);
    viewer2 = mock(Viewer.class);
    composite = new CompositeViewer<Object>(viewer1, viewer2);
  }

  public void testDelegatesGetView() {
    Object item = new Object();

    View view1 = mock(View.class);
    given(viewer1.getView(item, null, null)).willReturn(view1);

    View view2 = mock(View.class);
    given(viewer2.getView(item, view1, null)).willReturn(view2);

    assertThat(composite.getView(item, null, null)).isEqualTo(view2);
  }
}
