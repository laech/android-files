package l.files.ui.info.action;

import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Collection;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.info.InfoFragment;
import l.files.ui.info.InfoMultiFragment;
import l.files.ui.info.R;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.info.InfoBaseFragment.FRAGMENT_TAG;

public final class InfoAction extends ActionModeItem implements Selection.Callback {

    private final Selection<?, FileInfo> selection;
    private final FragmentManager manager;
    private final Path dir;

    public InfoAction(
            Selection<?, FileInfo> selection,
            FragmentManager manager,
            Path dir) {
        super(R.id.info);
        this.selection = requireNonNull(selection);
        this.manager = requireNonNull(manager);
        this.dir = requireNonNull(dir);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        mode.getMenuInflater().inflate(R.menu.info, menu);
        selection.addWeaklyReferencedCallback(this);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        super.onDestroyActionMode(mode);
        selection.removeCallback(this);
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        Collection<FileInfo> values = selection.values();
        if (values.size() == 1) {
            FileInfo file = values.iterator().next();
            Stat stat = file.selfStat();
            InfoFragment.create(file.selfPath(), stat).show(manager, FRAGMENT_TAG);
        } else {
            InfoMultiFragment.create(dir, values).show(manager, FRAGMENT_TAG);
        }
        mode.finish();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        super.onPrepareActionMode(mode, menu);
        updateMenuItem(menu);
        return true;
    }

    @Override
    public void onSelectionChanged() {
        if (mode != null) {
            updateMenuItem(mode.getMenu());
        }
    }

    private void updateMenuItem(Menu menu) {
        MenuItem item = menu.findItem(id());
        if (item == null) {
            return;
        }

        for (FileInfo file : selection.values()) {
            if (file.linkTargetOrSelfStat() != null) {
                item.setEnabled(true);
                return;
            }
        }
        item.setEnabled(false);
    }
}
