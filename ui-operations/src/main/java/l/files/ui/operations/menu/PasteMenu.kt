package l.files.ui.operations.menu

import android.app.Activity
import android.view.Menu
import android.view.Menu.NONE
import android.view.MenuItem
import android.view.MenuItem.SHOW_AS_ACTION_NEVER
import l.files.operations.newCopyIntent
import l.files.operations.newMoveIntent
import l.files.ui.base.app.OptionsMenuAction
import l.files.ui.operations.action.Clipboard
import java.nio.file.Path

class PasteMenu(
  private val context: Activity,
  private val destination: Path
) : OptionsMenuAction(android.R.id.paste) {

  private val clipboard: Clipboard = Clipboard.INSTANCE

  override fun onCreateOptionsMenu(menu: Menu) {
    super.onCreateOptionsMenu(menu)
    menu.add(NONE, id(), NONE, android.R.string.paste)
      .setShowAsAction(SHOW_AS_ACTION_NEVER)
  }

  override fun onPrepareOptionsMenu(menu: Menu) {
    super.onPrepareOptionsMenu(menu)
    val item = menu.findItem(id()) ?: return
    item.isEnabled = clipboard.action() != null &&
      !clipboard.paths().any(destination::startsWith) // Can't paste into itself
  }

  override fun onItemSelected(item: MenuItem) {
    when (clipboard.action()) {
      Clipboard.Action.COPY -> context.startService(
        newCopyIntent(
          context,
          clipboard.paths(),
          destination
        )
      )
      Clipboard.Action.CUT -> {
        context.startService(
          newMoveIntent(
            context,
            clipboard.paths(),
            destination
          )
        )
        clipboard.clear()
      }
    }
  }
}
