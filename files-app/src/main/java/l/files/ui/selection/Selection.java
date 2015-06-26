package l.files.ui.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import static java.util.Collections.unmodifiableSet;

public final class Selection<T>
{
    private final Set<T> selection = new HashSet<>();
    private final WeakHashMap<Callback, Void> callbacks = new WeakHashMap<>();
    private final List<Callback> pendingCallbacks = new ArrayList<>();

    public void addWeaklyReferencedCallback(final Callback callback)
    {
        callbacks.put(callback, null);
    }

    public void removeCallback(final Callback callback)
    {
        callbacks.remove(callback);
    }

    private void notifyCallbacks()
    {
        // Make a copy to avoid ConcurrentModificationException
        // if a callback adds another callback when notified
        pendingCallbacks.addAll(callbacks.keySet());
        try
        {
            for (final Callback callback : pendingCallbacks)
            {
                callback.onSelectionChanged();
            }
        }
        finally
        {
            pendingCallbacks.clear();
        }
    }

    public void clear()
    {
        if (!selection.isEmpty())
        {
            selection.clear();
            notifyCallbacks();
        }
    }

    public int size()
    {
        return selection.size();
    }

    public boolean isEmpty()
    {
        return selection.isEmpty();
    }

    public boolean contains(final T item)
    {
        return selection.contains(item);
    }

    public Set<T> copy()
    {
        return unmodifiableSet(new HashSet<>(selection));
    }

    public void add(final T item)
    {
        if (selection.add(item))
        {
            notifyCallbacks();
        }
    }

    public void addAll(final Collection<T> items)
    {
        if (selection.addAll(items))
        {
            notifyCallbacks();
        }
    }

    public void toggle(final T item)
    {
        if (selection.contains(item))
        {
            selection.remove(item);
        }
        else
        {
            selection.add(item);
        }
        notifyCallbacks();
    }

    public interface Callback
    {
        void onSelectionChanged();
    }
}
