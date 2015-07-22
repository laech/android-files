package l.files.common.graphics;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class ScaledSize {
  ScaledSize() {
  }

  public abstract int originalWidth();

  public abstract int originalHeight();

  public abstract int scaledWidth();

  public abstract int scaledHeight();

  public abstract float scale();

  public static ScaledSize of(
      int originalWidth,
      int originalHeight,
      int scaledWidth,
      int scaledHeight,
      float scale) {

    if (originalWidth <= 0
        || originalHeight <= 0
        || scaledWidth <= 0
        || scaledHeight <= 0
        || scale <= 0) {
      throw new IllegalArgumentException();
    }

    return new AutoParcel_ScaledSize(
        originalWidth,
        originalHeight,
        scaledWidth,
        scaledHeight,
        scale);
  }
}
