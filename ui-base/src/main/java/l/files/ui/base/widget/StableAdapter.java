package l.files.ui.base.widget;

import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static java.util.Collections.unmodifiableList;

public abstract class StableAdapter<T, VH extends ViewHolder> extends Adapter<VH> {

    private long seq = 0;
    private final Map<Object, Long> ids = new WeakHashMap<>();
    private final List<T> items = new ArrayList<>();

    protected StableAdapter() {
        setHasStableIds(true);
    }

    @SuppressWarnings("unchecked")
    public final void setItems(List<? extends T> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public List<T> items() {
        return unmodifiableList(items);
    }

    @Override
    public long getItemId(int position) {
        Object object = getItemIdObjectAt(position);
        Long id = ids.get(object);
        if (id == null) {
            id = seq++;
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

    protected T getItem(int position) {
        return items.get(position);
    }

    public Object getItemIdObjectAt(int position) {
        return getItemIdObject(getItem(position));
    }

    protected abstract Object getItemIdObject(T item);
}
