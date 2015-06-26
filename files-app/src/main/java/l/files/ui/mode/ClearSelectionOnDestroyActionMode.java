package l.files.ui.mode;

import android.view.ActionMode;

import l.files.common.widget.ActionModeAdapter;
import l.files.ui.selection.Selection;

import static java.util.Objects.requireNonNull;

public final class ClearSelectionOnDestroyActionMode
        extends ActionModeAdapter
{
    private final Selection<?> selection;

    public ClearSelectionOnDestroyActionMode(final Selection<?> selection)
    {
        this.selection = requireNonNull(selection, "selector");
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode)
    {
        super.onDestroyActionMode(mode);
        selection.clear();
    }
}
