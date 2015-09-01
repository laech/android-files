package l.files.ui.preview;

import com.google.auto.value.AutoValue;

import l.files.fs.Instant;

@AutoValue
abstract class Snapshot<V> {

  abstract V get();

  abstract Instant time();

  static <V> Snapshot<V> of(V value, Instant mtime) {
    return new AutoValue_Snapshot<>(value, mtime);
  }

}
