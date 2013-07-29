package l.files.common.app;

import android.view.Menu;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class OptionsMenusTest extends TestCase {

  public void testNullToEmpty_notNull() {
    OptionsMenu menu = mock(OptionsMenu.class);
    assertSame(menu, OptionsMenus.nullToEmpty(menu));
  }

  public void testNullToEmpty_null() {
    assertNotNull(OptionsMenus.nullToEmpty(null));
  }

  public void testCompose() {
    new ComposeTester().test();
  }

  private static class ComposeTester {
    private final OptionsMenu action1;
    private final OptionsMenu action2;
    private final OptionsMenu composite;

    ComposeTester() {
      action1 = mock(OptionsMenu.class);
      action2 = mock(OptionsMenu.class);
      composite = OptionsMenus.compose(action1, action2);
    }

    void test() {
      testOnCreateIsDelegated();
      testOnPrepareIsDelegated();
      testOnCloseIsDelegated();
    }

    private void testOnCreateIsDelegated() {
      Menu menu = mock(Menu.class);
      composite.onCreate(menu);
      verify(action1).onCreate(menu);
      verify(action2).onCreate(menu);
    }

    private void testOnPrepareIsDelegated() {
      Menu menu = mock(Menu.class);
      composite.onPrepare(menu);
      verify(action1).onPrepare(menu);
      verify(action2).onPrepare(menu);
    }

    private void testOnCloseIsDelegated() {
      Menu menu = mock(Menu.class);
      composite.onClose(menu);
      verify(action1).onClose(menu);
      verify(action2).onClose(menu);
    }
  }
}
