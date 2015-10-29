package l.files.ui.browser;

import android.view.Menu;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import l.files.ui.base.app.OptionsMenu;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = JELLY_BEAN)
public final class BaseActivityTest {

    private Menu menu;
    private OptionsMenu optionsMenu;
    private BaseActivity activity;

    @Before
    public void setUp() throws Exception {
        menu = mock(Menu.class);
        optionsMenu = mock(OptionsMenu.class);
        activity = new BaseActivity();
        activity.setOptionsMenu(optionsMenu);
    }

    @Test
    public void onCreateOptionsMenu_isDelegated() {
        activity.onCreateOptionsMenu(menu);
        verify(optionsMenu).onCreateOptionsMenu(menu);
    }

    @Test
    public void onPrepareOptionsMenu_isDelegated() {
        activity.onPrepareOptionsMenu(menu);
        verify(optionsMenu).onPrepareOptionsMenu(menu);
    }

    @Test
    public void callingMenuCallbacksWithoutSettingMenuWonNotCrash() {
        activity.onCreateOptionsMenu(null);
        activity.onPrepareOptionsMenu(null);
        activity.onOptionsMenuClosed(null);
    }

}
