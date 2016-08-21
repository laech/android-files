package l.files.ui.base.app;

import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.IdentityHashMap;
import java.util.Set;

import static java.util.Collections.newSetFromMap;

public class BaseFragment extends Fragment
        implements LifeCycleListenable {

    private OptionsMenu optionsMenu = OptionsMenus.EMPTY;


    private final Set<LifeCycleListener> lifeCycleListeners =
            newSetFromMap(new IdentityHashMap<LifeCycleListener, Boolean>(2));

    @Override
    public void addWeaklyReferencedLifeCycleListener(
            LifeCycleListener listener) {
        lifeCycleListeners.add(listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onDestroy();
        }
        lifeCycleListeners.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onPause();
        }
    }

    public final void setOptionsMenu(final OptionsMenu menu) {
        optionsMenu = OptionsMenus.nullToEmpty(menu);
    }

    @Override
    public final void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        optionsMenu.onCreateOptionsMenu(menu);
    }

    @Override
    public final void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        optionsMenu.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsMenu.onOptionsItemSelected(item);
    }
}
