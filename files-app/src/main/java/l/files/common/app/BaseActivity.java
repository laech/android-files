package l.files.common.app;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.common.view.ActionModeProvider;

public class BaseActivity extends Activity implements ActionModeProvider {

    private OptionsMenu optionsMenu = OptionsMenus.EMPTY;
    private Menu menu;

    private ActionMode currentActionMode;
    private ActionMode.Callback currentActionModeCallback;

    public Menu getMenu() {
        return menu;
    }

    public final void setOptionsMenu(OptionsMenu menu) {
        optionsMenu = OptionsMenus.nullToEmpty(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.optionsMenu.onCreateOptionsMenu(menu);
        this.menu = menu;
        return true;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        this.menu = null;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        this.optionsMenu.onPrepareOptionsMenu(menu);
        this.menu = menu;
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
