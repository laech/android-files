package l.files.ui.info

import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.info_size.*
import l.files.fs.Name
import l.files.fs.Path

abstract class InfoBaseFragment : AppCompatDialogFragment() {

  val displayedSize: CharSequence get() = sizeView.text

  private val sizeModel: CalculateSizeViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(layoutResourceId(), container, false)

  @LayoutRes
  protected abstract fun layoutResourceId(): Int

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    sizeModel.target.value = Target(readParentDirectory(), readChildren())
    sizeModel.calculation.observe(viewLifecycleOwner, Observer {
      sizeView.text = formatSize(it.size, it.count)
      calculateSizeProgressBar.visibility = if (it.done) GONE else VISIBLE
    })
  }

  protected fun readParentDirectory(): Path =
    requireArguments().getParcelable(ARG_PARENT_DIRECTORY)!!

  protected fun readChildren(): Set<Name> =
    requireArguments().getParcelableArrayList<Name>(ARG_CHILDREN)!!.toSet()

  protected fun formatSize(size: Long): String =
    Formatter.formatFileSize(activity, size)

  open fun formatSize(size: Long, count: Int): String =
    resources.getQuantityString(
      R.plurals.x_size_y_items,
      count,
      formatSize(size),
      count
    )

  companion object {
    const val FRAGMENT_TAG = "info-dialog"
    const val ARG_PARENT_DIRECTORY = "parentDirectory"
    const val ARG_CHILDREN = "children"
  }
}
