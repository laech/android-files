package l.files.ui.base.app;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import l.files.ui.base.view.ActionModeProvider;

import static l.files.base.Objects.requireNonNull;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity
        implements ActionModeProvider {

    private OptionsMenu optionsMenu = OptionsMenus.EMPTY;

    @Nullable
    private ActionMode currentActionMode;

    @Nullable
    private ActionMode.Callback currentActionModeCallback;

    public final void setOptionsMenu(OptionsMenu menu) {
        optionsMenu = requireNonNull(menu, "menu");
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
        currentActionMode = super.startSupportActionMode(callback);
        return currentActionMode;
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
