package l.files.common.app;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;

public final class OptionsMenusTest extends TestCase {

  public void testNullToEmpty_notNull() {
    OptionsMenu menu = mock(OptionsMenu.class);
    assertSame(menu, OptionsMenus.nullToEmpty(menu));
  }

  public void testNullToEmpty_null() {
    assertNotNull(OptionsMenus.nullToEmpty(null));
  }
}
