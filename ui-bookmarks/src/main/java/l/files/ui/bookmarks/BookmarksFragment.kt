package l.files.ui.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bookmarks_fragment.view.*
import l.files.base.lifecycle.SetLiveData
import l.files.bookmarks.collate
import l.files.bookmarks.getBookmarks
import l.files.ui.base.fs.OpenFileEvent
import l.files.ui.base.fs.UserDirs.DIR_HOME
import l.files.ui.base.selection.SelectionModeFragment
import l.files.ui.base.view.ActionModeProvider
import l.files.ui.base.view.ActionModes
import l.files.ui.base.view.ClearSelectionOnDestroyActionMode
import l.files.ui.base.view.CountSelectedItemsAction
import java.nio.file.Path

class BookmarksFragment :
  SelectionModeFragment<Path, Path>(),
  Observer<List<Path>> {

  lateinit var recycler: RecyclerView
  private lateinit var adapter: BookmarksAdapter
  private lateinit var bookmarks: SetLiveData<Path>

  fun bookmarks(): List<Path> = adapter.items()
    .filterIsInstance<Path>()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(R.layout.bookmarks_fragment, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    bookmarks = getBookmarks()
    bookmarks
      .map { it.collate({ path -> path == DIR_HOME }) }
      .observe(viewLifecycleOwner, this)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    adapter = BookmarksAdapter(
      selection(),
      actionModeProvider(),
      actionModeCallback(),
      OpenFileEvent.topic
    )
    recycler = requireView().bookmarks
    recycler.layoutManager = LinearLayoutManager(activity)
    recycler.adapter = adapter
  }

  override fun actionModeCallback(): ActionMode.Callback =
    ActionModes.compose(
      CountSelectedItemsAction(selection()),
      ClearSelectionOnDestroyActionMode(selection()),
      RemoveBookmark(selection(), bookmarks)
    )

  override fun actionModeProvider() = activity as ActionModeProvider

  override fun onChanged(bookmarks: List<Path>) {
    adapter.setItems(listOf(getString(R.string.bookmarks)) + bookmarks)
  }
}
