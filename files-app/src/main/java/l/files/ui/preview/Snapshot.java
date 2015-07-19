package l.files.ui.preview;

import auto.parcel.AutoParcel;
import l.files.fs.Instant;

@AutoParcel
abstract class Snapshot<V> {

  abstract V get();

  abstract Instant time();

  static <V> Snapshot<V> of(V value, Instant mtime) {
    return new AutoParcel_Snapshot<>(value, mtime);
  }
}
