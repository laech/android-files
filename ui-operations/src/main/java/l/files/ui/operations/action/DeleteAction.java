package l.files.ui.operations.action;

import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.FragmentManager;
import l.files.fs.Path;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.operations.R;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.stream.Collectors.toList;
import static l.files.base.Objects.requireNonNull;

public final class DeleteAction extends ActionModeItem {

    private final Selection<Path, ?> selection;
    private final FragmentManager manager;

    public DeleteAction(
        Selection<Path, ?> selection, FragmentManager manager
    ) {
        super(R.id.delete);
        this.manager = requireNonNull(manager);
        this.selection = requireNonNull(selection);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        menu.add(NONE, id(), NONE, R.string.delete)
            .setShowAsAction(SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        new DeleteDialog(selection.keys()
            .stream()
            .map(Path::toJavaPath)
            .collect(toList()), mode).show(manager, DeleteDialog.FRAGMENT_TAG);
    }

}
