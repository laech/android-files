package l.files.ui.operations.menu;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.Path;
import l.files.ui.base.app.OptionsMenuAction;
import l.files.ui.operations.action.Clipboard;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.base.Objects.requireNonNull;
import static l.files.operations.OperationService.newCopyIntent;
import static l.files.operations.OperationService.newMoveIntent;
import static l.files.ui.operations.action.Clipboard.Action.COPY;
import static l.files.ui.operations.action.Clipboard.Action.CUT;

public final class PasteMenu extends OptionsMenuAction {

    private final Path destination;
    private final Activity context;
    private final Clipboard clipboard;

    public PasteMenu(Activity context, Path destination) {
        super(android.R.id.paste);
        this.context = requireNonNull(context, "context");
        this.destination = requireNonNull(destination, "destination");
        this.clipboard = Clipboard.INSTANCE;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(NONE, id(), NONE, android.R.string.paste)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(id());
        if (item == null) {
            return;
        }

        item.setEnabled(clipboard.action() != null);
        for (Path path : clipboard.paths()) {
            if (destination.startsWith(path)) {
                // Can't paste into itself
                item.setEnabled(false);
                return;
            }
        }

    }

    @Override
    protected void onItemSelected(MenuItem item) {

        if (clipboard.action() == COPY) {
            context.startService(newCopyIntent(context, clipboard.paths(), destination));

        } else if (clipboard.action() == CUT) {
            context.startService(newMoveIntent(context, clipboard.paths(), destination));
            clipboard.clear();
        }
    }

}