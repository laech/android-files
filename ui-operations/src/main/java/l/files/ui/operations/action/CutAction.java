package l.files.ui.operations.action;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.premium.PremiumLock;
import l.files.fs.Path;
import l.files.premium.PremiumActionModeItem;
import l.files.ui.base.selection.Selection;
import l.files.ui.operations.R;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.operations.action.Clipboard.Action.CUT;

public final class CutAction extends PremiumActionModeItem {

    private final Selection<Path, ?> selection;

    public CutAction(
            PremiumLock premiumLock,
            Selection<Path, ?> selection
    ) {
        super(android.R.id.cut, premiumLock);
        this.selection = requireNonNull(selection);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        mode.getMenuInflater().inflate(R.menu.cut, menu);
        return true;
    }

    @Override
    protected void doOnItemSelected(ActionMode mode, MenuItem item) {
        Clipboard.INSTANCE.set(CUT, selection.keys());
        mode.finish();
    }

}
