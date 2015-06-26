package l.files.ui.selection;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ActionMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import javax.annotation.Nullable;

import l.files.common.view.ActionModeProvider;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static java.util.Objects.requireNonNull;

public abstract class SelectionModeViewHolder<T> extends ViewHolder
        implements OnClickListener, OnLongClickListener, Selection.Callback
{
    private final Selection<T> selection;
    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;

    public SelectionModeViewHolder(
            final View itemView,
            final Selection<T> selection,
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

    @Nullable
    private T item()
    {
        final int position = getAdapterPosition();
        return position == NO_POSITION ? null : item(position);
    }

    protected abstract T item(final int position);

    protected abstract void onClick(final View v, final T item);

    @Override
    public final void onSelectionChanged()
    {
        final T item = item();
        final boolean selected = selection.contains(item);
        if (itemView.isActivated() != selected && item != null)
        {
            itemView.setActivated(selected);
        }

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

    @Override
    public final void onClick(final View v)
    {
        final T item = item();
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
            selection.toggle(item);
        }
    }

    @Override
    public final boolean onLongClick(final View v)
    {
        final T item = item();
        if (item != null)
        {
            selection.toggle(item);
        }
        return true;
    }
}
