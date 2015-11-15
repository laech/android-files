package l.files.ui.preview;

import com.google.auto.value.AutoValue;

import l.files.fs.Instant;

@AutoValue
abstract class Snapshot<V> {

    abstract V get();

    abstract long time();

    static <V> Snapshot<V> of(V value, long time) {
        return new AutoValue_Snapshot<>(value, time);
    }

}
