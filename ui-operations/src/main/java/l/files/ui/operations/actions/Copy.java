package l.files.ui.operations.actions;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.premium.PremiumLock;
import l.files.fs.Path;
import l.files.premium.PremiumActionModeItem;
import l.files.ui.base.selection.Selection;
import l.files.ui.operations.R;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.operations.actions.Clipboard.Action.COPY;

public final class Copy extends PremiumActionModeItem {

    private final Selection<Path, ?> selection;

    public Copy(
            PremiumLock premiumLock,
            Selection<Path, ?> selection
    ) {
        super(android.R.id.copy, premiumLock);
        this.selection = requireNonNull(selection);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        mode.getMenuInflater().inflate(R.menu.copy, menu);
        return true;
    }

    @Override
    protected void doOnItemSelected(ActionMode mode, MenuItem item) {
        Clipboard.INSTANCE.set(COPY, selection.keys());
        mode.finish();
    }

}
