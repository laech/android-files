package l.files.ui.preview;

import static l.files.base.Objects.requireNonNull;

final class Snapshot<V> {

    private final V value;
    private final long time;

    private Snapshot(V value, long time) {
        this.value = requireNonNull(value);
        this.time = time;
    }

    V get() {
        return value;
    }

    long time() {
        return time;
    }

    static <V> Snapshot<V> of(V value, long time) {
        return new Snapshot<>(value, time);
    }

}
