package l.files.ui.operations.action

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.view.ActionMode
import l.files.fs.Path
import l.files.operations.newDeleteIntent
import l.files.ui.operations.R

class DeleteDialog internal constructor(
  // Null after screen rotation, in that case dismiss dialog
  private val paths: Collection<Path>?,
  private val mode: ActionMode?
) : AppCompatDialogFragment() {

  constructor() : this(null, null) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (paths == null || mode == null) {
      showsDialog = false
      dismissAllowingStateLoss()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog =
    AlertDialog.Builder(requireContext())
      .setMessage(getConfirmMessage(paths!!.size))
      .setNegativeButton(android.R.string.cancel, null)
      .setPositiveButton(R.string.delete) { _, _ ->
        requestDelete(paths)
        mode!!.finish()
      }
      .create()

  override fun getDialog() = super.getDialog() as AlertDialog

  private fun requestDelete(files: Collection<Path>) {
    val context = requireContext()
    context.startService(newDeleteIntent(context, files))
  }

  private fun getConfirmMessage(size: Int): String =
    requireContext().resources.getQuantityString(
      R.plurals.confirm_delete_question,
      size,
      size
    )

  companion object {
    const val FRAGMENT_TAG = "delete-dialog"
  }
}
