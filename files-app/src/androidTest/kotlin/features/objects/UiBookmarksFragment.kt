package l.files.features.objects

import android.app.Instrumentation
import l.files.fs.Path
import kotlin.test.assertEquals
import android.widget.ListView
import java.util.ArrayList
import kotlin.test.assertTrue
import l.files.ui.bookmarks.BookmarksFragment
import l.files.ui.FilesActivity
import l.files.R

class UiBookmarksFragment(
        private val instrument: Instrumentation,
        private val fragment: BookmarksFragment) {

    private val listView: ListView get() = fragment.getView() as ListView
    private val activity: FilesActivity get() = fragment.getActivity() as FilesActivity

    val activityObject = UiFileActivity(instrument, activity)

    fun selectBookmark(path: Path): UiFileActivity {
        instrument.awaitOnMainThread {
            listView.click { it.equals(path) }
        }
        if (path.resource.readStatus(false).isDirectory) {
            activityObject.assertCurrentDirectory(path)
        }
        return activityObject
    }

    fun checkBookmark(bookmark: Path, checked: Boolean): UiBookmarksFragment {
        instrument.awaitOnMainThread {
            val i = listView.indexOf { it.equals(bookmark) }
            listView.setItemChecked(i, checked)
        }
        return this
    }

    fun deleteCheckedBookmarks(): UiBookmarksFragment {
        activityObject.selectActionModeAction(R.id.delete_selected_bookmarks)
        activityObject.waitForActionModeToFinish()
        return this
    }

    private fun getBookmarks(): List<Path> {
        val paths = ArrayList<Path>()
        for (i in listView.getHeaderViewsCount()..listView.getCount() - 1) {
            paths.add(listView.getItemAtPosition(i) as Path)
        }
        return paths
    }

    fun assertCurrentDirectoryBookmarked(bookmarked: Boolean) = instrument.awaitOnMainThread {
        val path = activity.getCurrentPagerFragment().getCurrentPath()
        val paths = getBookmarks()
        assertEquals(bookmarked, paths.contains(path), paths.toString())
        this
    }

    fun assertBookmarked(bookmark: Path, bookmarked: Boolean) = instrument.awaitOnMainThread {
        assertEquals(bookmarked, getBookmarks().contains(bookmark))
        this
    }

    fun assertContainsBookmarksInOrder(vararg paths: Path): UiBookmarksFragment {
        instrument.awaitOnMainThread {
            assertEquals(paths.toList(), getBookmarks().filter { paths.contains(it) })
        }
        return this;
    }

    private fun ListView.indexOf(pred: (Any) -> Boolean): Int {
        for (i in getHeaderViewsCount()..getCount() - 1) {
            if (pred(getItemAtPosition(i))) {
                return i
            }
        }
        throw AssertionError("Not found")
    }

    private fun ListView.click(pred: (Any) -> Boolean) {
        val position = listView.indexOf(pred)
        val firstVisiblePosition = listView.getFirstVisiblePosition()
        val viewPosition = position - firstVisiblePosition
        val view = listView.getChildAt(viewPosition)
        assertTrue(listView.performItemClick(view, viewPosition, position.toLong()))
    }

}
