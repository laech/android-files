package l.files.common.widget;

import android.test.AndroidTestCase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public final class EnableStateDecoratorTest extends AndroidTestCase {

  public void testEnablesViewTreeIfPredicateReturnsTrue() {
    testEnable(true);
  }

  public void testDisablesViewTreeIfPredicateReturnsFalse() {
    testEnable(false);
  }

  private void testEnable(boolean enable) {
    View child = new TextView(getContext());
    ViewGroup parent = new LinearLayout(getContext());
    parent.addView(child);
    parent.setEnabled(true);
    child.setEnabled(true);

    decorator(Predicates.alwaysFalse()).decorate(parent, "x");

    assertFalse(parent.isEnabled());
    assertFalse(child.isEnabled());
  }

  private <T> EnableStateDecorator<T> decorator(Predicate<T> pred) {
    return new EnableStateDecorator<T>(pred);
  }
}
