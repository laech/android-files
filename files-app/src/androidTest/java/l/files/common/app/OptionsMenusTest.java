package l.files.common.app;

import l.files.common.testing.BaseTest;

import static org.mockito.Mockito.mock;

public final class OptionsMenusTest extends BaseTest {

    public void testNullToEmpty_notNull() {
        OptionsMenu menu = mock(OptionsMenu.class);
        assertSame(menu, OptionsMenus.nullToEmpty(menu));
    }

    public void testNullToEmpty_null() {
        assertNotNull(OptionsMenus.nullToEmpty(null));
    }
}
