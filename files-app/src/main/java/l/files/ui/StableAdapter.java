package l.files.ui;

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public abstract class StableAdapter<T, VH extends ViewHolder> extends Adapter<VH> {
    private final Map<Object, Long> ids = new HashMap<>();

    private List<T> items = emptyList();

    public StableAdapter() {
        setHasStableIds(true);
    }

    @SuppressWarnings("unchecked")
    public void setItems(final List<? extends T> items) {
        this.items = (List<T>) requireNonNull(items);
        notifyDataSetChanged();
    }

    public List<T> items() {
        return unmodifiableList(items);
    }

    @Override
    public long getItemId(final int position) {
        final Object object = getItemIdObject(position);
        Long id = ids.get(object);
        if (id == null) {
            id = ids.size() + 1L;
            ids.put(object, id);
        }
        return id;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public T getItem(final int position) {
        return items.get(position);
    }

    public abstract Object getItemIdObject(final int position);
}
