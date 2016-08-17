package l.files.ui.browser.menu;

import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import l.files.ui.base.app.OptionsMenuAdapter;

import static android.support.v4.view.GravityCompat.START;
import static l.files.base.Objects.requireNonNull;

public final class ActionBarDrawerToggleMenu extends OptionsMenuAdapter {

    private final DrawerLayout drawer;
    private final FragmentManager fragments;

    public ActionBarDrawerToggleMenu(
            DrawerLayout drawer,
            FragmentManager fragments) {
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
