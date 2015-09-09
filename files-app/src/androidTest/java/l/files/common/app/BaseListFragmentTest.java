package l.files.common.app;

import android.os.Looper;
import android.view.Menu;

import l.files.common.testing.BaseTest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class BaseListFragmentTest extends BaseTest {
    private Menu menu;
    private OptionsMenu optionsMenu;
    private BaseListFragment fragment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        menu = mock(Menu.class);
        optionsMenu = mock(OptionsMenu.class);
        fragment = new BaseListFragment();
        fragment.setOptionsMenu(optionsMenu);
    }

    public void testOnCreateOptionsMenu_isDelegated() {
        fragment.onCreateOptionsMenu(menu, null);
        verify(optionsMenu).onCreateOptionsMenu(menu);
    }

    public void testOnPrepareOptionsMenu_isDelegated() {
        fragment.onPrepareOptionsMenu(menu);
        verify(optionsMenu).onPrepareOptionsMenu(menu);
    }

    public void testCallingMenuCallbacksWithoutSettingMenuWonNotCrash() {
        final BaseListFragment fragment = new BaseListFragment();
        fragment.onCreateOptionsMenu(null, null);
        fragment.onPrepareOptionsMenu(null);
        fragment.onOptionsMenuClosed(null);
    }
}
