package l.files.ui.preview

import android.graphics.drawable.ColorDrawable

/**
 * A color drawable with fixed width and height.
 */
class SizedColorDrawable(
  color: Int,
  private val width: Int,
  private val height: Int
) : ColorDrawable(color) {

  override fun getIntrinsicHeight(): Int = height
  override fun getIntrinsicWidth(): Int = width

}
