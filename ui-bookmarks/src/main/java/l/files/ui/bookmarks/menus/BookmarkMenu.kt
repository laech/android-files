package l.files.ui.bookmarks.menus

import android.view.Menu
import android.view.Menu.NONE
import android.view.MenuItem
import l.files.base.lifecycle.SetLiveData
import l.files.ui.base.app.OptionsMenuAction
import l.files.ui.bookmarks.R
import java.nio.file.Path

class BookmarkMenu(
  private val path: Path,
  private val bookmarks: SetLiveData<Path>
) : OptionsMenuAction(R.id.bookmark) {

  override fun onCreateOptionsMenu(menu: Menu) {
    super.onCreateOptionsMenu(menu)
    menu.add(NONE, id(), NONE, R.string.bookmark).isCheckable = true
  }

  override fun onPrepareOptionsMenu(menu: Menu) {
    super.onPrepareOptionsMenu(menu)
    menu.findItem(id())?.isChecked = bookmarks.contains(path)
  }

  public override fun onItemSelected(item: MenuItem) {
    val checked = item.isChecked
    item.isChecked = !checked
    if (checked) {
      bookmarks.remove(path)
    } else {
      bookmarks.add(path)
    }
  }
}
