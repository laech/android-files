package l.files.ui.selection;

import android.os.Bundle;
import android.view.ActionMode;

import l.files.common.app.BaseFragment;
import l.files.common.view.ActionModeProvider;

public abstract class SelectionModeFragment<T> extends BaseFragment
{
    private Selection<T> selection;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        selection = new Selection<>();
    }

    @Override
    public void onViewStateRestored(final Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);
        if (!selection.isEmpty())
        {
            actionModeProvider().startActionMode(actionModeCallback());
        }
    }

    protected Selection<T> selection()
    {
        return selection;
    }

    protected abstract ActionMode.Callback actionModeCallback();

    protected abstract ActionModeProvider actionModeProvider();
}
