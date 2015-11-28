package l.files.ui.browser;

import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.browser.BrowserItem.FileItem;

import static l.files.base.Objects.requireNonNull;

public final class Info extends ActionModeItem implements Selection.Callback {

    private final Selection<?, FileItem> selection;
    private final FragmentManager manager;

    public Info(Selection<?, FileItem> selection, FragmentManager manager) {
        super(R.id.info);
        this.selection = requireNonNull(selection);
        this.manager = requireNonNull(manager);
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
        FileItem fileItem = selection.values().iterator().next();
        File file = fileItem.selfFile();
        Stat stat = fileItem.linkTargetOrSelfStat();
        if (stat != null) {
            InfoFragment.create(file, stat).show(manager, InfoFragment.FRAGMENT_TAG);
        }
        selection.removeCallback(this);
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
        item.setVisible(selection.size() == 1 &&
                selection.values().iterator()
                        .next().linkTargetOrSelfStat() != null);
    }
}
