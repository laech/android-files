package l.files.common.widget;

import android.view.View;
import junit.framework.TestCase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public final class NullableDecoratorTest extends TestCase {

  private int viewId;
  private Decorator<Object> delegate;
  private NullableDecorator<Object> decorator;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    viewId = 2;
    delegate = mock(Decorator.class);
    decorator = new NullableDecorator<Object>(viewId, delegate);
  }

  public void testDoesNothingIfViewNotFound() {
    View view = mock(View.class);
    given(view.findViewById(viewId)).willReturn(null);

    decorator.decorate(view, "x");

    verifyZeroInteractions(delegate);
  }

  public void testCallsDelegateIfViewFound() {
    View view = mock(View.class);
    View notNull = mock(View.class);
    given(view.findViewById(viewId)).willReturn(notNull);

    decorator.decorate(view, "x");

    verify(delegate).decorate(view, "x");
  }
}
