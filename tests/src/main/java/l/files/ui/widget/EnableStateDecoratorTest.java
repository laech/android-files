package l.files.ui.widget;

import android.view.View;
import com.google.common.base.Predicate;
import junit.framework.TestCase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class EnableStateDecoratorTest extends TestCase {

  private int viewId;
  private Predicate<Object> predicate;
  private EnableStateDecorator<Object> decorator;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    viewId = 1;
    predicate = mock(Predicate.class);
    decorator = new EnableStateDecorator<Object>(viewId, predicate);
  }

  public void testEnablesView() {
    test(true);
  }

  public void testDisablesView() {
    test(false);
  }

  private void test(boolean enable) {
    View view = mock(View.class);
    given(view.findViewById(viewId)).willReturn(view);
    given(predicate.apply("x")).willReturn(enable);

    decorator.decorate(view, "x");

    verify(view).setEnabled(enable);
  }
}
