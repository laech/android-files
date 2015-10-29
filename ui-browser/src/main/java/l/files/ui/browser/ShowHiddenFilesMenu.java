package l.files.ui.browser;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import l.files.ui.base.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

final class ShowHiddenFilesMenu extends OptionsMenuAction {

    private final Context context;

    ShowHiddenFilesMenu(Context context) {
        super(R.id.show_hidden_files);
        this.context = requireNonNull(context);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(NONE, id(), NONE, R.string.show_hidden_files)
                .setCheckable(true)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.show_hidden_files);
        if (item != null) {
            item.setChecked(Preferences.getShowHiddenFiles(context));
        }
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        Preferences.setShowHiddenFiles(context, !item.isChecked());
    }
}
