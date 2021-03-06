package l.files.ui.base.view;

import android.view.Menu;
import androidx.appcompat.view.ActionMode;
import l.files.ui.base.selection.Selection;

import static java.util.Objects.requireNonNull;

public final class CountSelectedItemsAction
    extends ActionModeAdapter
    implements Selection.Callback {

    private final Selection<?, ?> selection;

    public CountSelectedItemsAction(Selection<?, ?> selection) {
        this.selection = requireNonNull(selection);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        updateSelectedItemCount();
        this.selection.addWeaklyReferencedCallback(this);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        super.onDestroyActionMode(mode);
        this.selection.removeCallback(this);
    }

    @Override
    public void onSelectionChanged() {
        updateSelectedItemCount();
    }

    private void updateSelectedItemCount() {
        if (mode != null) {
            mode.setTitle(Integer.toString(selection.size()));
        }
    }
}
