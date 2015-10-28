package l.files.ui.base.app;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public final class OptionsMenusTest {

    @Test
    public void nullToEmpty_notNull() {
        OptionsMenu menu = mock(OptionsMenu.class);
        assertSame(menu, OptionsMenus.nullToEmpty(menu));
    }

    @Test
    public void nullToEmpty_null() {
        assertNotNull(OptionsMenus.nullToEmpty(null));
    }
}
