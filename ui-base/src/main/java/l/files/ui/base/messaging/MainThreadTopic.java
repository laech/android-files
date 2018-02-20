package l.files.ui.base.messaging;

import android.support.annotation.MainThread;

import java.util.Set;
import java.util.WeakHashMap;

import l.files.base.Consumer;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static java.util.Collections.newSetFromMap;

@MainThread
public final class MainThreadTopic<T> {

    private final Set<Consumer<T>> listeners = newSetFromMap(new WeakHashMap<>());

    private static void ensureMainThread() {
        if (myLooper() != getMainLooper()) {
            throw new IllegalStateException("Can only be called on the main thread.");
        }
    }

    public void weakSubscribeOnMainThread(Consumer<T> listener) {
        ensureMainThread();
        listeners.add(listener);
    }

    public void unsubscribeOnMainThread(Consumer<T> listener) {
        ensureMainThread();
        listeners.remove(listener);
    }

    public void postOnMainThread(T message) {
        ensureMainThread();
        for (Consumer<T> listener : listeners) {
            listener.accept(message);
        }
    }
}
