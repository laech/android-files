package l.files.common.base;

import android.support.annotation.Nullable;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class Either<A, B> {
  Either() {
  }

  @Nullable public abstract A left();

  @Nullable public abstract B right();

  public static <A, B> Either<A, B> left(final A value) {
    return new AutoParcel_Either<>(value, null);
  }

  public static <A, B> Either<A, B> right(final B value) {
    return new AutoParcel_Either<>(null, value);
  }

}
