package l.files.ui.bookmarks

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem

import l.files.R
import l.files.common.widget.MultiChoiceModeAction
import l.files.fs.Path
import l.files.provider.bookmarks.BookmarkManager
import l.files.ui.ListSelection

import android.view.Menu.NONE

private class DeleteAction(

        private val bookmarks: BookmarkManager,
        private val selections: ListSelection<Path>

) : MultiChoiceModeAction(R.id.delete_selected_bookmarks) {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        menu.add(NONE, id(), NONE, R.string.delete)
        return true
    }

    override fun onItemSelected(mode: ActionMode, item: MenuItem) {
        bookmarks.removeBookmarks(selections.getCheckedItems())
        mode.finish()
    }

}
