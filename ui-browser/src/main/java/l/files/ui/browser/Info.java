package l.files.ui.browser;

import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Collection;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.browser.BrowserItem.FileItem;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.browser.InfoBaseFragment.FRAGMENT_TAG;

public final class Info extends ActionModeItem implements Selection.Callback {

    private final Selection<?, FileItem> selection;
    private final FragmentManager manager;
    private final File dir;

    public Info(Selection<?, FileItem> selection, FragmentManager manager, File dir) {
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
        Collection<FileItem> values = selection.values();
        if (values.size() == 1) {
            FileItem file = values.iterator().next();
            Stat stat = file.linkTargetOrSelfStat();
            InfoFragment.create(file.selfFile(), stat).show(manager, FRAGMENT_TAG);
        } else {
            InfoMultiFragment.create(dir, values).show(manager, FRAGMENT_TAG);
        }
        mode.finish();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        super.onPrepareActionMode(mode, menu);
        updateVisibility(menu);
        return true;
    }

    @Override
    public void onSelectionChanged() {
        if (mode != null) {
            updateVisibility(mode.getMenu());
        }
    }

    private void updateVisibility(Menu menu) {
        MenuItem item = menu.findItem(id());
        if (item == null) {
            return;
        }

        for (FileItem file : selection.values()) {
            if (file.linkTargetOrSelfStat() != null) {
                item.setVisible(true);
                return;
            }
        }
        item.setVisible(false);
    }
}
