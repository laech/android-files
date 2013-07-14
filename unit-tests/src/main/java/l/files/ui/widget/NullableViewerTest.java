package l.files.ui.widget;

import android.view.View;
import junit.framework.TestCase;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public final class NullableViewerTest extends TestCase {

  private int viewId;
  private Viewer<Object> delegate;
  private NullableViewer<Object> viewer;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    viewId = 2;
    delegate = mock(Viewer.class);
    viewer = new NullableViewer<Object>(viewId, delegate);
  }

  public void testReturnsIfViewNotFound() {
    View view = mock(View.class);
    given(view.findViewById(viewId)).willReturn(null);

    assertThat(viewer.getView("x", view, null)).isSameAs(view);
    verifyZeroInteractions(delegate);
  }

  public void testCallsDelegateIfViewFound() {
    View arg = mock(View.class);
    View expected = mock(View.class);
    View notNull = mock(View.class);
    given(arg.findViewById(viewId)).willReturn(notNull);
    given(delegate.getView("x", arg, null)).willReturn(expected);

    View actual = viewer.getView("x", arg, null);
    verify(delegate).getView("x", arg, null);
    assertThat(actual).isEqualTo(expected);
  }
}
