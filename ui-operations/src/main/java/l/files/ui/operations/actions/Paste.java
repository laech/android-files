package l.files.ui.operations.actions;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.File;
import l.files.ui.base.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;
import static l.files.operations.OperationService.newCopyIntent;
import static l.files.operations.OperationService.newMoveIntent;
import static l.files.ui.operations.actions.Clipboard.Action.COPY;
import static l.files.ui.operations.actions.Clipboard.Action.CUT;

public final class Paste extends OptionsMenuAction {

    private final File destination;
    private final Activity context;
    private final Clipboard clipboard;

    public Paste(Activity context, File destination) {
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
        for (File file : clipboard.files()) {
            if (this.destination.pathStartsWith(file)) {
                // Can't paste into itself
                item.setEnabled(false);
                return;
            }
        }

    }

    @Override
    protected void onItemSelected(MenuItem item) {

        if (clipboard.action() == COPY) {
            context.startService(newCopyIntent(context, clipboard.files(), destination));

        } else if (clipboard.action() == CUT) {
            context.startService(newMoveIntent(context, clipboard.files(), destination));
            clipboard.clear();
        }
    }

}
