package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import static android.widget.AbsListView.MultiChoiceModeListener;
import static java.util.Objects.requireNonNull;

public class MultiChoiceModeListenerDelegate implements MultiChoiceModeListener {

    private final MultiChoiceModeListener delegate;

    public MultiChoiceModeListenerDelegate(MultiChoiceModeListener delegate) {
        this.delegate = requireNonNull(delegate, "delegate");
    }

    @Override
    public void onItemCheckedStateChanged(
            ActionMode mode, int position, long id, boolean checked) {
        delegate.onItemCheckedStateChanged(mode, position, id, checked);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return delegate.onCreateActionMode(mode, menu);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return delegate.onPrepareActionMode(mode, menu);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return delegate.onActionItemClicked(mode, item);
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        delegate.onDestroyActionMode(mode);
    }
}
