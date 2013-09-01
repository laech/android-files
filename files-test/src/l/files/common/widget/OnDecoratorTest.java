package l.files.common.widget;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.test.AndroidTestCase;
import android.view.View;

public final class OnDecoratorTest extends AndroidTestCase {

  private int id;
  private Decorator<Object> delegate;
  private OnDecorator<Object> decorator;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    id = 101;
    delegate = mock(Decorator.class);
    decorator = new OnDecorator<Object>(id, delegate);
  }

  public void testCallsSubViewToDelegate() {
    View parent = mock(View.class);
    View child = mock(View.class);
    given(parent.findViewById(id)).willReturn(child);

    decorator.decorate(parent, "abc");

    verify(delegate).decorate(child, "abc");
  }
}
