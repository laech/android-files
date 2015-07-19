package l.files.common.graphics;

import auto.parcel.AutoParcel;

import static com.google.common.base.Preconditions.checkArgument;

@AutoParcel
public abstract class ScaledBound {
  ScaledBound() {
  }

  public abstract Bound original();

  public abstract Bound scaled();

  public abstract float scale();

  public static ScaledBound of(Bound original, Bound scaled, float scale) {
    checkArgument(scale > 0);
    return new AutoParcel_ScaledBound(original, scaled, scale);
  }
}
