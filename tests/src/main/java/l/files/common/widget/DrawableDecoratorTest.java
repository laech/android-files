package l.files.common.widget;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import com.google.common.base.Function;
import junit.framework.TestCase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class DrawableDecoratorTest extends TestCase {

  private int textViewId;
  private Function<Object, Drawable> drawables;
  private DrawableDecorator<Object> decorator;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    textViewId = 1;
    drawables = mock(Function.class);
    decorator = new DrawableDecorator<Object>(textViewId, drawables);
  }

  public void testSetsDrawableToTextView() {
    Object item = new Object();
    Drawable drawable = setDrawable(item);
    TextView view = setTextView();

    decorator.decorate(view, item);

    verify(view).findViewById(textViewId);
    verify(view).setCompoundDrawablesWithIntrinsicBounds(
        drawable, null, null, null);
  }

  private TextView setTextView() {
    TextView view = mock(TextView.class);
    given(view.findViewById(textViewId)).willReturn(view);
    return view;
  }

  private Drawable setDrawable(Object item) {
    Drawable drawable = new ColorDrawable();
    given(drawables.apply(item)).willReturn(drawable);
    return drawable;
  }
}
