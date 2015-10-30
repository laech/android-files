package l.files.ui.browser;

import android.support.v4.widget.DrawerLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.ui.browser.FilesActivity.DrawerListener;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.support.v4.view.GravityCompat.START;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = JELLY_BEAN)
public final class FilesActivityTest {

    @Test
    public void closes_sidebar_on_open_file() throws Exception {
        FilesActivity activity = activityWithMockDrawer();
        given(activity.drawerLayout().isDrawerOpen(START)).willReturn(true);
        activity.onOpen(mock(File.class), mock(Stat.class));
        verify(activity.drawerLayout()).closeDrawers();
    }

    @Test
    public void lock_closed_sidebar_on_start_action_mode() throws Exception {
        FilesActivity activity = activityWithMockDrawer();
        activity.onActionModeStarted(null);
        verify(activity.drawerLayout()).setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
    }

    @Test
    public void lock_opened_sidebar_on_start_action_mode() throws Exception {
        FilesActivity activity = activityWithMockDrawer();
        given(activity.drawerLayout().isDrawerOpen(START)).willReturn(true);
        activity.onActionModeStarted(null);
        verify(activity.drawerLayout()).setDrawerLockMode(LOCK_MODE_LOCKED_OPEN);
    }

    @Test
    public void unlock_sidebar_on_finish_action_mode() throws Exception {
        FilesActivity activity = activityWithMockDrawer();
        activity.onActionModeFinished(null);
        verify(activity.drawerLayout()).setDrawerLockMode(LOCK_MODE_UNLOCKED);
    }

    private FilesActivity activityWithMockDrawer() {
        FilesActivity activity = new FilesActivity();
        activity.setDrawerLayout(mock(DrawerLayout.class));
        activity.setDrawerListener(mock(DrawerListener.class));
        return activity;
    }

}
