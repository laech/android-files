package l.files.ui.base.view;

import android.view.ActionMode;

import l.files.ui.base.selection.Selection;

import static java.util.Objects.requireNonNull;

public final class ClearSelectionOnDestroyActionMode
        extends ActionModeAdapter {
    private final Selection<?> selection;

    public ClearSelectionOnDestroyActionMode(final Selection<?> selection) {
        this.selection = requireNonNull(selection, "selector");
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        super.onDestroyActionMode(mode);
        selection.clear();
    }
}
