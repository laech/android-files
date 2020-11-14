package l.files.ui.info.action

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.FragmentManager
import l.files.ui.base.fs.FileInfo
import l.files.ui.base.selection.Selection
import l.files.ui.base.view.ActionModeItem
import l.files.ui.info.InfoBaseFragment
import l.files.ui.info.InfoFragment
import l.files.ui.info.InfoMultiFragment
import l.files.ui.info.R
import java.nio.file.Path

class InfoAction(
  private val selection: Selection<*, FileInfo>,
  private val manager: FragmentManager,
  private val parentDirectory: Path
) : ActionModeItem(R.id.info), Selection.Callback {

  override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
    super.onCreateActionMode(mode, menu)
    mode.menuInflater.inflate(R.menu.info, menu)
    selection.addWeaklyReferencedCallback(this)
    return true
  }

  override fun onDestroyActionMode(mode: ActionMode) {
    super.onDestroyActionMode(mode)
    selection.removeCallback(this)
  }

  override fun onItemSelected(mode: ActionMode, item: MenuItem) {
    val values = selection.values()
    if (values.size == 1) {
      showSingleFileInfo(values)
    } else {
      showMultiFileInfo(values)
    }
    mode.finish()
  }

  private fun showMultiFileInfo(values: Collection<FileInfo>) {
    InfoMultiFragment.create(parentDirectory, values)
      .show(manager, InfoBaseFragment.FRAGMENT_TAG)
  }

  private fun showSingleFileInfo(values: Collection<FileInfo>) {
    val file = values.iterator().next()
    val stat = file.selfStat()
    InfoFragment.create(file.selfPath().toJavaPath(), stat)
      .show(manager, InfoBaseFragment.FRAGMENT_TAG)
  }

  override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
    super.onPrepareActionMode(mode, menu)
    updateMenuItem(menu)
    return true
  }

  override fun onSelectionChanged() {
    if (mode != null) {
      updateMenuItem(mode!!.menu)
    }
  }

  private fun updateMenuItem(menu: Menu) {
    val item = menu.findItem(id()) ?: return
    item.isEnabled =
      selection.values().any { it.linkTargetOrSelfStat() != null }
  }
}
