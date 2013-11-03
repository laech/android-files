package l.files.common.widget;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.widget.TextView;
import com.google.common.base.Function;
import l.files.test.BaseTest;

public final class TextDecoratorTest extends BaseTest {

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
