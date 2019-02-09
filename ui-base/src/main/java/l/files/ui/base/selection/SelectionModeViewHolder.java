package l.files.ui.base.selection;

import androidx.appcompat.view.ActionMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import java.util.List;

import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.widget.ItemViewHolder;

import static l.files.base.Objects.requireNonNull;

public abstract class SelectionModeViewHolder<K, V> extends ItemViewHolder<V>
        implements OnClickListener, OnLongClickListener, Selection.Callback {

    private final Selection<K, V> selection;
    private final ActionModeProvider actionModeProvider;
    private final ActionMode.Callback actionModeCallback;

    public SelectionModeViewHolder(
            View itemView,
            Selection<K, V> selection,
            ActionModeProvider actionModeProvider,
            ActionMode.Callback actionModeCallback
    ) {
        super(itemView);

        this.actionModeProvider = requireNonNull(actionModeProvider);
        this.actionModeCallback = requireNonNull(actionModeCallback);
        this.selection = requireNonNull(selection);
        this.selection.addWeaklyReferencedCallback(this);
        this.itemView.setOnClickListener(this);
        this.itemView.setOnLongClickListener(this);
    }

    protected abstract K itemId(V item);

    @Override
    public void bind(V item, List<Object> payloads) {
        super.bind(item, payloads);
        setActivated(item);
    }

    @Override
    public void onSelectionChanged() {
        V item = item();
        if (item == null) {
            return;
        }
        setActivated(item);

        ActionMode mode = actionModeProvider.currentActionMode();
        if (mode != null && selection.isEmpty()) {
            mode.finish();
        } else if (mode == null && !selection.isEmpty()) {
            actionModeProvider.startSupportActionMode(actionModeCallback);
        }
    }

    private void setActivated(V item) {
        K itemId = itemId(item);
        boolean selected = selection.contains(itemId);
        if (itemView.isActivated() != selected) {
            itemView.setActivated(selected);
        }
    }

    @Override
    public void onClick(View v) {
        V item = item();
        if (item == null) {
            return;
        }

        if (actionModeProvider.currentActionMode() == null) {
            onClick(v, item);
        } else {
            selection.toggle(itemId(item), item);
        }
    }

    protected abstract void onClick(View v, V item);

    @Override
    public boolean onLongClick(View v) {
        V item = item();
        selection.toggle(itemId(item), item);
        return true;
    }
}
