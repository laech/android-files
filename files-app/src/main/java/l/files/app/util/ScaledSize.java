package l.files.app.util;

import static com.google.common.base.Preconditions.checkArgument;

public final class ScaledSize {

  public final int scaledWidth;
  public final int scaledHeight;

  /**
   * The scale of this size, between 0 and 1.
   */
  public final float scale;

  ScaledSize(int scaledWidth, int scaledHeight, float scale) {
    checkArgument(scaledWidth > 0);
    checkArgument(scaledHeight > 0);
    checkArgument(scale > 0);
    checkArgument(scale <= 1);
    this.scaledWidth = scaledWidth;
    this.scaledHeight = scaledHeight;
    this.scale = scale;
  }
}
