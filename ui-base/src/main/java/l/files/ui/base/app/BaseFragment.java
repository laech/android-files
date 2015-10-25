package l.files.ui.base.app;

import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BaseFragment extends Fragment {
    private OptionsMenu optionsMenu = OptionsMenus.EMPTY;

    public final void setOptionsMenu(final OptionsMenu menu) {
        optionsMenu = OptionsMenus.nullToEmpty(menu);
    }

    @Override
    public final void onCreateOptionsMenu(
            final Menu menu,
            final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        optionsMenu.onCreateOptionsMenu(menu);
    }

    @Override
    public final void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        optionsMenu.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return optionsMenu.onOptionsItemSelected(item);
    }
}
