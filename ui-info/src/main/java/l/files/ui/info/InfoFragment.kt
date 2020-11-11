package l.files.ui.info

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.format.DateUtils.*
import android.view.View
import kotlinx.android.synthetic.main.info_fragment.*
import l.files.fs.Stat
import l.files.ui.base.graphics.Rect
import l.files.ui.preview.Preview
import l.files.ui.preview.getPreview
import java.nio.file.Path

class InfoFragment : InfoBaseFragment(), Preview.Callback {

  private var stat: Stat? = null
  private lateinit var path: Path
  private lateinit var constraint: Rect

  val displayedName: CharSequence
    get() = nameView.text

  val displayedLastModifiedTime: CharSequence
    get() = lastModifiedView.text

  override fun layoutResourceId() = R.layout.info_fragment

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    constraint = calculateConstraint()
    path = readParentDirectory().resolve(readChildren().iterator().next())
    stat = requireArguments().getParcelable(ARG_STAT)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    updateViews()
  }

  private fun updateViews() {
    updateNameView(path)
    if (stat != null) {
      updateBackgroundView(path, stat!!)
      updateThumbnailView(path, stat!!)
      updateLastModifiedView(stat!!)
    }
  }

  private fun updateNameView(path: Path) {
    val name = path.fileName
    nameView.maxWidth = constraint.width()
    nameView.text = name?.toString() ?: path.toString()
  }

  override fun formatSize(size: Long, count: Int): String =
    if (stat == null || stat!!.isDirectory) {
      super.formatSize(size, count)
    } else {
      formatSize(size)
    }

  private fun updateLastModifiedView(stat: Stat) {
    val millis = stat.lastModifiedTime().toEpochMilli()
    val flags = FORMAT_SHOW_DATE or FORMAT_SHOW_TIME
    val text = formatDateTime(activity, millis, flags)
    lastModifiedView.text = text
  }

  private fun calculateConstraint(): Rect {
    val metrics = resources.displayMetrics
    val width = (metrics.widthPixels * 0.75).toInt()
    val height = metrics.heightPixels
    return Rect.of(width, height)
  }

  private fun updateBackgroundView(path: Path, stat: Stat) {
    val preview = requireContext().getPreview()
    val bg = preview.getBlurredThumbnail(path, stat, constraint, true)
    bg?.let { updateBackgroundView(it) }
  }

  private fun updateBackgroundView(bitmap: Bitmap) {
    val bg = BitmapDrawable(resources, bitmap)
    bg.alpha = (0.3f * 255).toInt()
    backgroundView.background = bg
  }

  private fun updateThumbnailView(path: Path, stat: Stat) {
    val context = requireContext()
    val preview = context.getPreview()
    val thumbnail =
      preview.getThumbnail(path, stat, constraint, true)
    if (thumbnail != null) {
      thumbnailView.setImageBitmap(thumbnail)
      return
    }
    val size =
      preview.getSize(path, stat, constraint, false)
    size?.let { setImageViewMinSize(it) }
    preview.get(path, stat, constraint, this, context)
  }

  private fun scaleSize(size: Rect): Rect = size.scaleDown(constraint)

  private fun setImageViewMinSize(size: Rect) {
    val scaled = scaleSize(size)
    thumbnailView.minimumWidth = scaled.width()
    thumbnailView.minimumHeight = scaled.height()
  }

  override fun onPreviewAvailable(path: Path, stat: Stat, thumbnail: Bitmap) {
    showImageView(thumbnail)
  }

  private fun showImageView(thumbnail: Bitmap) {
    thumbnailView.setImageBitmap(thumbnail)
    thumbnailView.alpha = 0f
    thumbnailView.animate().alpha(1f).duration = animationDuration().toLong()
  }

  private fun animationDuration() =
    resources.getInteger(android.R.integer.config_mediumAnimTime)

  override fun onBlurredThumbnailAvailable(
    path: Path,
    stat: Stat,
    thumbnail: Bitmap
  ) {
  }

  override fun onPreviewFailed(path: Path, stat: Stat, cause: Any) =
    hindImageView()

  private fun hindImageView() {
    thumbnailView.setImageDrawable(null)
    thumbnailView.visibility = View.GONE
  }

  companion object {
    private const val ARG_STAT = "stat"

    @JvmStatic
    fun create(path: Path, stat: Stat?): InfoFragment =
      newFragment(newArgs(path, stat))

    private fun newArgs(path: Path, stat: Stat?): Bundle {
      val bundle = Bundle()
      bundle.putStringArrayList(
        ARG_CHILDREN,
        arrayListOf(path.fileName.toString())
      )
      bundle.putString(ARG_PARENT_DIRECTORY, path.parent.toString())
      bundle.putParcelable(ARG_STAT, stat)
      return bundle
    }

    private fun newFragment(bundle: Bundle): InfoFragment {
      val fragment = InfoFragment()
      fragment.arguments = bundle
      return fragment
    }
  }
}
