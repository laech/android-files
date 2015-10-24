package l.files.ui.base.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import static java.util.Collections.unmodifiableSet;

public final class Selection<T> implements Iterable<T> {

    private final Set<T> selection = new HashSet<>();
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

    public boolean contains(T item) {
        return selection.contains(item);
    }

    public Set<T> copy() {
        return unmodifiableSet(new HashSet<>(selection));
    }

    public void add(T item) {
        if (selection.add(item)) {
            notifyCallbacks();
        }
    }

    public void addAll(Collection<T> items) {
        if (selection.addAll(items)) {
            notifyCallbacks();
        }
    }

    public void retainAll(Collection<T> items) {
        if (selection.retainAll(items)) {
            notifyCallbacks();
        }
    }

    public void toggle(T item) {
        if (selection.contains(item)) {
            selection.remove(item);
        } else {
            selection.add(item);
        }
        notifyCallbacks();
    }

    @Override
    public Iterator<T> iterator() {
        return unmodifiableSet(selection).iterator();
    }

    public interface Callback {
        void onSelectionChanged();
    }
}
