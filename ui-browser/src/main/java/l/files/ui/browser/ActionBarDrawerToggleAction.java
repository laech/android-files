package l.files.ui.browser;

import android.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import l.files.ui.base.app.OptionsMenuAdapter;

import static android.support.v4.view.GravityCompat.START;
import static java.util.Objects.requireNonNull;

final class ActionBarDrawerToggleAction extends OptionsMenuAdapter {

    private final DrawerLayout drawer;
    private final FragmentManager fragments;

    ActionBarDrawerToggleAction(
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
