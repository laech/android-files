package l.files.ui.browser;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.ui.base.app.OptionsMenu;
import l.files.ui.base.app.OptionsMenus;
import l.files.ui.base.view.ActionModeProvider;

public class BaseActivity extends AppCompatActivity implements ActionModeProvider {

    private OptionsMenu optionsMenu = OptionsMenus.EMPTY;

    private ActionMode currentActionMode;
    private ActionMode.Callback currentActionModeCallback;

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
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
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
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        currentActionMode = null;
        currentActionModeCallback = null;
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        currentActionModeCallback = callback;
        return (currentActionMode = super.startActionMode(callback));
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
