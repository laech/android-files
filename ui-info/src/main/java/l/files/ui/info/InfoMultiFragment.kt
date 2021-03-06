package l.files.ui.info

import android.os.Bundle
import l.files.ui.base.fs.FileInfo
import java.nio.file.Path

class InfoMultiFragment : InfoBaseFragment() {

  override fun layoutResourceId(): Int = R.layout.info_multi_fragment

  companion object {

    @JvmStatic
    fun create(
      parentDirectory: Path,
      children: Collection<FileInfo>
    ): InfoMultiFragment =
      newFragment(newArgs(parentDirectory, children))

    private fun newArgs(
      parentDirectory: Path,
      items: Collection<FileInfo>
    ): Bundle {
      val bundle = Bundle()
      bundle.putString(ARG_PARENT_DIRECTORY, parentDirectory.toString())
      bundle.putStringArrayList(
        ARG_CHILDREN,
        items.mapTo(ArrayList(items.size)) {
          it.selfPath().fileName.toString() /* TODO handle null */
        })
      return bundle
    }

    private fun newFragment(bundle: Bundle): InfoMultiFragment {
      val fragment = InfoMultiFragment()
      fragment.arguments = bundle
      return fragment
    }
  }
}
