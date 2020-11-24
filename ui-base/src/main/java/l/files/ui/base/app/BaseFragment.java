package l.files.ui.base.app;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.fragment.app.Fragment;

import java.util.IdentityHashMap;
import java.util.Set;

import static java.util.Collections.newSetFromMap;
import static java.util.Objects.requireNonNull;

public class BaseFragment extends Fragment
    implements LifeCycleListenable {

    private OptionsMenu optionsMenu = OptionsMenus.EMPTY;


    private final Set<LifeCycleListener> lifeCycleListeners =
        newSetFromMap(new IdentityHashMap<>(2));

    @Override
    public void addWeaklyReferencedLifeCycleListener(
        LifeCycleListener listener
    ) {
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

    public final void setOptionsMenu(OptionsMenu menu) {
        optionsMenu = requireNonNull(menu);
    }

    @Override
    public final void onCreateOptionsMenu(
        Menu menu, MenuInflater inflater
    ) {
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
