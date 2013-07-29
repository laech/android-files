package l.files.common.widget;

import android.widget.TextView;
import com.google.common.base.Function;
import junit.framework.TestCase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class TextDecoratorTest extends TestCase {

  private int textViewId;
  private Function<Object, String> labels;
  private TextDecorator<Object> decorator;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    textViewId = 2;
    labels = mock(Function.class);
    decorator = new TextDecorator<Object>(textViewId, labels);
  }

  public void testSetsTextToTextView() {
    Object item = new Object();
    given(labels.apply(item)).willReturn("hello");
    TextView view = setTextView();

    decorator.decorate(view, item);

    verify(view).findViewById(textViewId);
    verify(view).setText("hello");
  }

  private TextView setTextView() {
    TextView view = mock(TextView.class);
    given(view.findViewById(textViewId)).willReturn(view);
    return view;
  }
}
