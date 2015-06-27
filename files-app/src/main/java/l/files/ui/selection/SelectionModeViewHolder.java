package l.files.ui.selection;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ActionMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import l.files.common.view.ActionModeProvider;

import static java.util.Objects.requireNonNull;

public abstract class SelectionModeViewHolder<ID, ITEM> extends ViewHolder
        implements OnClickListener, OnLongClickListener, Selection.Callback
{
    private final Selection<ID> selection;
    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;

    private ITEM item;

    public SelectionModeViewHolder(
            final View itemView,
            final Selection<ID> selection,
            final ActionModeProvider actionModeProvider,
            final ActionMode.Callback actionModeCallback)
    {
        super(itemView);
        this.actionModeProvider = requireNonNull(actionModeProvider, "actionModeProvider");
        this.actionModeCallback = requireNonNull(actionModeCallback, "actionModeCallback");
        this.selection = requireNonNull(selection, "selection");
        this.selection.addWeaklyReferencedCallback(this);
        this.itemView.setOnClickListener(this);
        this.itemView.setOnLongClickListener(this);
    }

    protected abstract ID itemId(ITEM item);

    public void bind(final ITEM item)
    {
        this.item = item;
        setActivated(item);
    }

    @Override
    public final void onSelectionChanged()
    {
        if (item == null)
        {
            return;
        }
        setActivated(item);

        final ActionMode mode = actionModeProvider.currentActionMode();
        if (mode != null && selection.isEmpty())
        {
            mode.finish();
        }
        else if (mode == null && !selection.isEmpty())
        {
            actionModeProvider.startActionMode(actionModeCallback);
        }
    }

    private void setActivated(final ITEM item)
    {
        final ID itemId = itemId(item);
        final boolean selected = selection.contains(itemId);
        if (itemView.isActivated() != selected && itemId != null)
        {
            itemView.setActivated(selected);
        }
    }

    @Override
    public final void onClick(final View v)
    {
        if (item == null)
        {
            return;
        }

        if (actionModeProvider.currentActionMode() == null)
        {
            onClick(v, item);
        }
        else
        {
            selection.toggle(itemId(item));
        }
    }

    protected abstract void onClick(final View v, final ITEM item);

    @Override
    public final boolean onLongClick(final View v)
    {
        selection.toggle(itemId(item));
        return true;
    }
}
