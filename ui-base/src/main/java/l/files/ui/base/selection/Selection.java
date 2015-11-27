package l.files.ui.base.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

public final class Selection<K, V> implements Iterable<K> {

    private final Map<K, V> selection = new HashMap<>();
    private final WeakHashMap<Callback, Void> callbacks = new WeakHashMap<>();

    public void addWeaklyReferencedCallback(Callback callback) {
        callbacks.put(callback, null);
    }

    public void removeCallback(Callback callback) {
        callbacks.remove(callback);
    }

    private void notifyCallbacks() {
        for (Callback callback : new ArrayList<>(callbacks.keySet())) {
            callback.onSelectionChanged();
        }
    }

    public void clear() {
        if (!selection.isEmpty()) {
            selection.clear();
            notifyCallbacks();
        }
    }

    public int size() {
        return selection.size();
    }

    public boolean isEmpty() {
        return selection.isEmpty();
    }

    public boolean contains(K item) {
        return selection.containsKey(item);
    }

    public Map<K, V> copy() {
        return unmodifiableMap(new HashMap<>(selection));
    }

    public Collection<K> keys() {
        return selection.keySet();
    }

    public Collection<V> values() {
        return selection.values();
    }

    public void add(K item, V data) {
        if (!data.equals(selection.put(item, data))) {
            notifyCallbacks();
        }
    }

    public void addAll(Map<K, V> items) {

        boolean changed = false;

        for (Map.Entry<K, V> entry : items.entrySet()) {
            K item = entry.getKey();
            V data = entry.getValue();
            if (!data.equals(selection.put(item, data))) {
                changed = true;
            }
        }

        if (changed) {
            notifyCallbacks();
        }
    }

    public void retainAll(Collection<K> items) {
        if (selection.keySet().retainAll(items)) {
            notifyCallbacks();
        }
    }

    public void toggle(K item, V data) {
        if (selection.containsKey(item)) {
            selection.remove(item);
        } else {
            selection.put(item, data);
        }
        notifyCallbacks();
    }

    @Override
    public Iterator<K> iterator() {
        return unmodifiableSet(selection.keySet()).iterator();
    }

    public interface Callback {
        void onSelectionChanged();
    }
}
