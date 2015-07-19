package l.files.common.graphics;

import auto.parcel.AutoParcel;

import static com.google.common.base.Preconditions.checkArgument;

@AutoParcel
public abstract class Bound {
  Bound() {
  }

  public abstract int width();

  public abstract int height();

  public boolean contains(Bound that) {
    return width() >= that.width() && height() >= that.height();
  }

  public static Bound of(int width, int height) {
    checkArgument(width > 0);
    checkArgument(height > 0);
    return new AutoParcel_Bound(width, height);
  }

}
