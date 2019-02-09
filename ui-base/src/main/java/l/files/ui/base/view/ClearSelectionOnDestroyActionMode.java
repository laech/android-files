package l.files.ui.base.view;

import androidx.appcompat.view.ActionMode;

import l.files.ui.base.selection.Selection;

import static l.files.base.Objects.requireNonNull;

public final class ClearSelectionOnDestroyActionMode
        extends ActionModeAdapter {

    private final Selection<?, ?> selection;

    public ClearSelectionOnDestroyActionMode(Selection<?, ?> selection) {
        this.selection = requireNonNull(selection);
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        super.onDestroyActionMode(mode);
        selection.clear();
    }
}
