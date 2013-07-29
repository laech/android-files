package l.files.common.widget;

import android.view.View;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class CompositeDecoratorTest extends TestCase {

  private Decorator<Object> delegate1;
  private Decorator<Object> delegate2;

  private CompositeDecorator<Object> composite;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    delegate1 = mock(Decorator.class);
    delegate2 = mock(Decorator.class);
    composite = new CompositeDecorator<Object>(delegate1, delegate2);
  }

  public void testCallsDelegates() {
    Object item = new Object();
    View view = mock(View.class);
    composite.decorate(view, item);
    verify(delegate1).decorate(view, item);
    verify(delegate2).decorate(view, item);
  }
}
