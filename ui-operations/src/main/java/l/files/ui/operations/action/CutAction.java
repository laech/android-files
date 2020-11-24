package l.files.ui.operations.action;

import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.view.ActionMode;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.operations.R;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;
import static l.files.ui.operations.action.Clipboard.Action.CUT;

public final class CutAction extends ActionModeItem {

    private final Selection<Path, ?> selection;

    public CutAction(Selection<Path, ?> selection) {
        super(android.R.id.cut);
        this.selection = requireNonNull(selection);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        mode.getMenuInflater().inflate(R.menu.cut, menu);
        return true;
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        Clipboard.INSTANCE.set(CUT, selection.keys());
        mode.finish();
    }

}
