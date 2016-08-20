package l.files.ui.base.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.IdentityHashMap;
import java.util.Set;

import l.files.ui.base.view.ActionModeProvider;

import static java.util.Collections.newSetFromMap;

public class BaseActivity extends AppCompatActivity implements ActionModeProvider {

    private OptionsMenu optionsMenu = OptionsMenus.EMPTY;

    private ActionMode currentActionMode;
    private ActionMode.Callback currentActionModeCallback;

    private final Set<LifeCycleListener> lifeCycleListeners =
            newSetFromMap(new IdentityHashMap<LifeCycleListener, Boolean>(2));

    public void addWeaklyReferencedLifeCycleListener(LifeCycleListener listener) {
        lifeCycleListeners.add(listener);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onCreate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onPause();
        }
    }

    public final void setOptionsMenu(OptionsMenu menu) {
        optionsMenu = OptionsMenus.nullToEmpty(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.optionsMenu.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        this.optionsMenu.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsMenu.onOptionsItemSelected(item);
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        currentActionMode = null;
        currentActionModeCallback = null;
    }

    @Override
    public ActionMode startSupportActionMode(@NonNull ActionMode.Callback callback) {
        currentActionModeCallback = callback;
        return (currentActionMode = super.startSupportActionMode(callback));
    }

    @Nullable
    @Override
    public ActionMode currentActionMode() {
        return currentActionMode;
    }

    @Nullable
    public ActionMode.Callback currentActionModeCallback() {
        return currentActionModeCallback;
    }
}
