package l.files.ui.browser.menu;

import android.view.MenuItem;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import l.files.ui.base.app.OptionsMenuAdapter;

import static androidx.core.view.GravityCompat.START;
import static java.util.Objects.requireNonNull;

public final class ActionBarDrawerToggleMenu extends OptionsMenuAdapter {

    private final DrawerLayout drawer;
    private final FragmentManager fragments;

    public ActionBarDrawerToggleMenu(
        DrawerLayout drawer,
        FragmentManager fragments
    ) {
        this.drawer = requireNonNull(drawer);
        this.fragments = requireNonNull(fragments);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home &&
            fragments.getBackStackEntryCount() == 0) {
            if (drawer.isDrawerVisible(START)) {
                drawer.closeDrawer(START);
            } else {
                drawer.openDrawer(START);
            }
            return true;
        }
        return false;
    }
}
