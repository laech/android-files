package l.files.ui.browser.menu;

import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.Path;
import l.files.premium.PremiumLock;
import l.files.premium.PremiumOptionsMenuAction;
import l.files.ui.browser.R;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.base.Objects.requireNonNull;

public final class NewDirMenu extends PremiumOptionsMenuAction {

    private final Path directory;
    private final FragmentManager manager;

    public NewDirMenu(
            PremiumLock premiumLock,
            FragmentManager manager,
            Path directory
    ) {
        super(R.id.new_dir, premiumLock);
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
    protected void doOnItemSelected(MenuItem item) {
        NewDirFragment.create(directory).show(manager, NewDirFragment.TAG);
    }
}
