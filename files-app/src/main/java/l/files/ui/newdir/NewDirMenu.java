package l.files.ui.newdir;

import android.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.app.OptionsMenuAction;
import l.files.fs.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

public final class NewDirMenu extends OptionsMenuAction
{

    private final File directory;
    private final FragmentManager manager;

    public NewDirMenu(final FragmentManager manager, final File directory)
    {
        super(R.id.new_dir);
        this.manager = requireNonNull(manager, "manager");
        this.directory = requireNonNull(directory, "directory");
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(NONE, id(), NONE, R.string.new_dir)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
    }

    @Override
    protected void onItemSelected(final MenuItem item)
    {
        NewDirFragment.create(directory).show(manager, NewDirFragment.TAG);
    }
}
