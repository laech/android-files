package l.files.ui.browser.menu;

import android.view.Menu;
import android.view.MenuItem;
import androidx.fragment.app.FragmentManager;
import l.files.ui.base.app.OptionsMenuAction;
import l.files.ui.browser.R;

import java.nio.file.Path;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.base.Objects.requireNonNull;

public final class NewDirMenu extends OptionsMenuAction {

    private final Path directory;
    private final FragmentManager manager;

    public NewDirMenu(
        FragmentManager manager,
        Path directory
    ) {
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
