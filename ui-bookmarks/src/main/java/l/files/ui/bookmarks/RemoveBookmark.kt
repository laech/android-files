package l.files.ui.bookmarks

import android.view.Menu
import android.view.Menu.NONE
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import l.files.base.lifecycle.SetLiveData
import l.files.ui.base.selection.Selection
import l.files.ui.base.view.ActionModeItem
import java.nio.file.Path

internal class RemoveBookmark(
  private val selections: Selection<Path, *>,
  private val bookmarks: SetLiveData<Path>
) : ActionModeItem(R.id.delete_selected_bookmarks) {

  override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
    super.onCreateActionMode(mode, menu)
    menu.add(NONE, id(), NONE, R.string.remove)
      .setIcon(R.drawable.ic_remove_circle_outline_white_24dp)
    return true
  }

  public override fun onItemSelected(mode: ActionMode, item: MenuItem) {
    bookmarks.removeAll(selections.keys())
    mode.finish()
  }

}
