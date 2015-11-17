package l.files.ui.base.view;

import android.support.v7.view.ActionMode;
import android.view.Menu;

import l.files.ui.base.selection.Selection;

import static l.files.base.Objects.requireNonNull;

public final class CountSelectedItemsAction
        extends ActionModeAdapter
        implements Selection.Callback {

    private final Selection<?> selection;

    public CountSelectedItemsAction(Selection<?> selection) {
        this.selection = requireNonNull(selection, "selector");
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
