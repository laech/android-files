package l.files.common.widget;

import android.widget.TextView;
import com.google.common.base.Function;
import junit.framework.TestCase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class TextDecoratorTest extends TestCase {

  private Function<Object, String> labels;
  private TextDecorator<Object> decorator;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    labels = mock(Function.class);
    decorator = new TextDecorator<Object>(labels);
  }

  public void testSetsTextToTextView() {
    given(labels.apply("a")).willReturn("hello");
    TextView view = mock(TextView.class);

    decorator.decorate(view, "a");

    verify(view).setText("hello");
  }
}
