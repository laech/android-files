package l.files.common.widget;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import junit.framework.TestCase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public final class DecoratorsTest extends TestCase {

  public void testCompose() {
    new ComposeTester().test();
  }

  public void testText() {
    new TextTester().test();
  }

  public void testNullable() {
    new NullableTester().test();
  }

  public void testEnable() {
    new EnableTester().test();
  }

  public void testDraw() {
    new DrawTester().test();
  }

  private static class ComposeTester {
    private final Decorator<Object> delegate1;
    private final Decorator<Object> delegate2;
    private final Decorator<Object> composition;

    @SuppressWarnings("unchecked") ComposeTester() {
      delegate1 = mock(Decorator.class);
      delegate2 = mock(Decorator.class);
      composition = Decorators.compose(delegate1, delegate2);
    }

    void test() {
      testCallsDelegates();
    }

    private void testCallsDelegates() {
      Object item = new Object();
      View view = mock(View.class);
      composition.decorate(view, item);
      verify(delegate1).decorate(view, item);
      verify(delegate2).decorate(view, item);
    }
  }

  private static class EnableTester {
    private final int viewId;
    private final Predicate<Object> predicate;
    private final Decorator<Object> decorator;

    @SuppressWarnings("unchecked") EnableTester() {
      viewId = 1;
      predicate = mock(Predicate.class);
      decorator = Decorators.enable(viewId, predicate);
    }

    void test() {
      testEnable(true);
      testEnable(false);
    }

    private void testEnable(boolean enable) {
      View view = mock(View.class);
      given(view.findViewById(viewId)).willReturn(view);
      given(predicate.apply("x")).willReturn(enable);

      decorator.decorate(view, "x");

      verify(view).setEnabled(enable);
    }
  }

  public final class TextTester {
    private final int textViewId;
    private final Function<Object, String> labels;
    private final Decorator<Object> decorator;

    @SuppressWarnings("unchecked") TextTester() {
      textViewId = 2;
      labels = mock(Function.class);
      decorator = Decorators.text(textViewId, labels);
    }

    void test() {
      testSetsTextViewText();
    }

    private void testSetsTextViewText() {
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

  public final class NullableTester {
    private final int viewId;
    private final Decorator<Object> delegate;
    private final Decorator<Object> decorator;

    @SuppressWarnings("unchecked") NullableTester() {
      viewId = 2;
      delegate = mock(Decorator.class);
      decorator = Decorators.nullable(viewId, delegate);
    }

    void test() {
      testCallsDelegateIfViewFound();
      testDoesNothingIfViewNotFound();
    }

    private void testDoesNothingIfViewNotFound() {
      View view = mock(View.class);
      given(view.findViewById(viewId)).willReturn(null);

      decorator.decorate(view, "x");

      verifyZeroInteractions(delegate);
    }

    private void testCallsDelegateIfViewFound() {
      View view = mock(View.class);
      View notNull = mock(View.class);
      given(view.findViewById(viewId)).willReturn(notNull);

      decorator.decorate(view, "x");

      verify(delegate).decorate(view, "x");
    }
  }

  public final class DrawTester {
    private final int textViewId;
    private final Function<Object, Drawable> drawables;
    private final Decorator<Object> decorator;

    @SuppressWarnings("unchecked") DrawTester() {
      textViewId = 1;
      drawables = mock(Function.class);
      decorator = Decorators.draw(textViewId, drawables);
    }

    void test() {
      testSetsDrawableToTextView();
    }

    private void testSetsDrawableToTextView() {
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
}
