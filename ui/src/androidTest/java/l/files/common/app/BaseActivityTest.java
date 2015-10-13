package l.files.common.app;

import android.os.Looper;
import android.view.Menu;

import l.files.testing.BaseTest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class BaseActivityTest extends BaseTest {

    private Menu menu;
    private OptionsMenu optionsMenu;
    private BaseActivity activity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        menu = mock(Menu.class);
        optionsMenu = mock(OptionsMenu.class);
        activity = new BaseActivity();
        activity.setOptionsMenu(optionsMenu);
    }

    public void testOnCreateOptionsMenu_isDelegated() {
        activity.onCreateOptionsMenu(menu);
        verify(optionsMenu).onCreateOptionsMenu(menu);
    }

    public void testOnPrepareOptionsMenu_isDelegated() {
        activity.onPrepareOptionsMenu(menu);
        verify(optionsMenu).onPrepareOptionsMenu(menu);
    }

    public void testCallingMenuCallbacksWithoutSettingMenuWonNotCrash() {
        final BaseActivity activity = new BaseActivity();
        activity.onCreateOptionsMenu(null);
        activity.onPrepareOptionsMenu(null);
        activity.onOptionsMenuClosed(null);
    }
}
