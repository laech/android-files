package l.files.ui.browser;

import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.File;
import l.files.ui.R;
import l.files.ui.base.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

final class NewDirMenu extends OptionsMenuAction {

    private final File directory;
    private final FragmentManager manager;

    NewDirMenu(FragmentManager manager, File directory) {
        super(R.id.new_dir);
        this.manager = requireNonNull(manager, "manager");
        this.directory = requireNonNull(directory, "directory");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(NONE, id(), NONE, R.string.new_dir)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        NewDirFragment.create(directory).show(manager, NewDirFragment.TAG);
    }
}
