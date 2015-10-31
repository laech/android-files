package l.files.ui.base.selection;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ActionMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import l.files.ui.base.view.ActionModeProvider;

import static l.files.base.Objects.requireNonNull;

public abstract class SelectionModeViewHolder<ID, ITEM> extends ViewHolder
        implements OnClickListener, OnLongClickListener, Selection.Callback {

    private final Selection<ID> selection;
    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;

    private ITEM item;

    public SelectionModeViewHolder(
            View itemView,
            Selection<ID> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback) {
        super(itemView);
        this.actionModeProvider = requireNonNull(actionModeProvider, "actionModeProvider");
        this.actionModeCallback = requireNonNull(actionModeCallback, "actionModeCallback");
        this.selection = requireNonNull(selection, "selection");
        this.selection.addWeaklyReferencedCallback(this);
        this.itemView.setOnClickListener(this);
        this.itemView.setOnLongClickListener(this);
    }

    protected abstract ID itemId(ITEM item);

    @Nullable
    protected ID itemId() {
        return item == null ? null : itemId(item);
    }

    public void bind(ITEM item) {
        this.item = item;
        setActivated(item);
    }

    @Override
    public void onSelectionChanged() {
        if (item == null) {
            return;
        }
        setActivated(item);

        ActionMode mode = actionModeProvider.currentActionMode();
        if (mode != null && selection.isEmpty()) {
            mode.finish();
        } else if (mode == null && !selection.isEmpty()) {
            actionModeProvider.startActionMode(actionModeCallback);
        }
    }

    private void setActivated(ITEM item) {
        ID itemId = itemId(item);
        boolean selected = selection.contains(itemId);
        if (itemView.isActivated() != selected && itemId != null) {
            itemView.setActivated(selected);
        }
    }

    @Override
    public void onClick(View v) {
        if (item == null) {
            return;
        }

        if (actionModeProvider.currentActionMode() == null) {
            onClick(v, item);
        } else {
            selection.toggle(itemId(item));
        }
    }

    protected abstract void onClick(View v, ITEM item);

    @Override
    public boolean onLongClick(View v) {
        selection.toggle(itemId(item));
        return true;
    }
}
