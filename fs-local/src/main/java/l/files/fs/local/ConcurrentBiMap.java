package l.files.fs.local;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Collections.unmodifiableSet;

final class ConcurrentBiMap<A, B> {

    private final ConcurrentMap<A, B> ab = new ConcurrentHashMap<>();
    private final ConcurrentMap<B, A> ba = new ConcurrentHashMap<>();

    B get(A a) {
        return ab.get(a);
    }

    void put(A a, B b) {
        ab.put(a, b);
        ba.put(b, a);
    }

    B remove(A a) {
        B b = ab.remove(a);
        if (b != null) {
            ba.remove(b);
        }
        return b;
    }

    A remove2(B b) {
        A a = ba.remove(b);
        if (a != null) {
            ab.remove(a);
        }
        return a;
    }

    void clear() {
        ab.clear();
        ba.clear();
    }

    Set<A> keySet() {
        return unmodifiableSet(ab.keySet());
    }
}
