package l.files.common.app;

import android.view.Menu;

import l.files.common.testing.BaseTest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
    composite.onCreateOptionsMenu(menu);
    verify(action1).onCreateOptionsMenu(menu);
    verify(action2).onCreateOptionsMenu(menu);
  }

  public void testOnPrepareIsDelegated() {
    Menu menu = mock(Menu.class);
    composite.onPrepareOptionsMenu(menu);
    verify(action1).onPrepareOptionsMenu(menu);
    verify(action2).onPrepareOptionsMenu(menu);
  }
}
