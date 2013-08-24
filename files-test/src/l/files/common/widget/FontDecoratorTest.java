package l.files.common.widget;

import static android.graphics.Typeface.SANS_SERIF;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import com.google.common.base.Function;
import junit.framework.TestCase;

public final class FontDecoratorTest extends TestCase {

  private int textViewId;
  private Function<Object, Typeface> fonts;
  private FontDecorator<Object> decorator;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    textViewId = 101;
    fonts = mock(Function.class);
    decorator = new FontDecorator<Object>(textViewId, fonts);
  }

  public void testSetsTextViewTypeface() {
    TextView textView = mock(TextView.class);
    View container = mock(TextView.class);
    given(container.findViewById(textViewId)).willReturn(textView);
    given(fonts.apply("1")).willReturn(SANS_SERIF);

    decorator.decorate(container, "1");

    verify(textView).setTypeface(SANS_SERIF);
  }
}
