package l.files.common.app;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.view.Menu;
import l.files.test.BaseTest;

public final class CompositeMenuTest extends BaseTest {

  private OptionsMenu action1;
  private OptionsMenu action2;
  private CompositeMenu composite;

  @Override protected void setUp() throws Exception {
    super.setUp();
    action1 = mock(OptionsMenu.class);
    action2 = mock(OptionsMenu.class);
    composite = new CompositeMenu(action1, action2);
  }

  public void testOnCreateIsDelegated() {
    Menu menu = mock(Menu.class);
    composite.onCreate(menu);
    verify(action1).onCreate(menu);
    verify(action2).onCreate(menu);
  }

  public void testOnPrepareIsDelegated() {
    Menu menu = mock(Menu.class);
    composite.onPrepare(menu);
    verify(action1).onPrepare(menu);
    verify(action2).onPrepare(menu);
  }
}
