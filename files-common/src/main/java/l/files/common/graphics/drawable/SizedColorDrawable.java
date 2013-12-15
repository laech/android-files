package l.files.common.graphics.drawable;

import android.graphics.drawable.ColorDrawable;

/**
 * A color drawable with fixed width and height.
 */
public final class SizedColorDrawable extends ColorDrawable {

  private final int width;
  private final int height;

  public SizedColorDrawable(int color, int width, int height) {
    super(color);
    this.width = width;
    this.height = height;
  }

  @Override public int getIntrinsicHeight() {
    return height;
  }

  @Override public int getIntrinsicWidth() {
    return width;
  }
}
