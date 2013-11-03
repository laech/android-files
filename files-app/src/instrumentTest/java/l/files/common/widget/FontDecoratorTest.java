package l.files.common.widget;

import static android.graphics.Typeface.SANS_SERIF;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.graphics.Typeface;
import android.widget.TextView;
import com.google.common.base.Function;
import l.files.test.BaseTest;

public final class FontDecoratorTest extends BaseTest {

  private Function<Object, Typeface> fonts;
  private FontDecorator<Object> decorator;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    fonts = mock(Function.class);
    decorator = new FontDecorator<Object>(fonts);
  }

  public void testSetsTextViewTypeface() {
    TextView textView = mock(TextView.class);
    given(fonts.apply("1")).willReturn(SANS_SERIF);

    decorator.decorate(textView, "1");

    verify(textView).setTypeface(SANS_SERIF);
  }
}
