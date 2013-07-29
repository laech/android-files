package l.files.common.widget;

import android.content.Context;
import android.test.AndroidTestCase;
import android.view.View;
import android.widget.FrameLayout;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ViewersTest extends AndroidTestCase {

  public void testDecorate() {
    new DecorateTester(getContext()).test();
  }

  private static class DecorateTester {
    private final Context context;
    private final Viewer<Object> viewer;
    private final Decorator<Object> decorator;

    @SuppressWarnings("unchecked") DecorateTester(Context context) {
      this.context = context;
      decorator = mock(Decorator.class);
      viewer = Viewers.decorate(android.R.layout.simple_list_item_1, decorator);
    }

    void test() {
      testGetViewWillInflatesNewViewIfViewIsNull();
      testGetViewWillReturnsViewIfViewIsNotNull();
    }

    private void testGetViewWillInflatesNewViewIfViewIsNull() {
      View view = viewer.getView(null, null, new FrameLayout(context));
      assertThat(view).isNotNull();
      verify(decorator).decorate(view, null);
    }

    private void testGetViewWillReturnsViewIfViewIsNotNull() {
      Object item = new Object();
      View view = new View(context);
      View actual = viewer.getView(item, view, new FrameLayout(context));
      assertThat(actual).isSameAs(view);
      verify(decorator).decorate(view, item);
    }
  }
}
