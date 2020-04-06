package l.files.ui.bookmarks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ActionMode
import kotlinx.android.synthetic.main.bookmark_header.view.*
import kotlinx.android.synthetic.main.bookmark_item.view.*
import l.files.fs.Path
import l.files.ui.base.fs.FileIcons
import l.files.ui.base.fs.FileLabels
import l.files.ui.base.fs.OpenFileEvent
import l.files.ui.base.messaging.MainThreadTopic
import l.files.ui.base.selection.Selection
import l.files.ui.base.selection.SelectionModeViewHolder
import l.files.ui.base.view.ActionModeProvider
import l.files.ui.base.widget.ItemViewHolder
import l.files.ui.base.widget.StableAdapter

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_BOOKMARK = 1

internal class BookmarksAdapter(
    private val selection: Selection<Path, Path>,
    private val actionModeProvider: ActionModeProvider,
    private val actionModeCallback: ActionMode.Callback,
    private val topic: MainThreadTopic<OpenFileEvent>
) : StableAdapter<Any, ItemViewHolder<Any>>() {

    override fun getItemViewType(position: Int): Int = when {
        getItem(position) is Path -> VIEW_TYPE_BOOKMARK
        else -> VIEW_TYPE_HEADER
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        (when (viewType) {
            VIEW_TYPE_BOOKMARK -> newBookmarkHolder(parent)
            else -> newHeaderHolder(parent)
        }) as ItemViewHolder<Any>

    private fun newHeaderHolder(parent: ViewGroup) = HeaderHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.bookmark_header, parent, false)
    )

    private fun newBookmarkHolder(parent: ViewGroup) = BookmarkHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.bookmark_item, parent, false)
    )

    override fun onBindViewHolder(holder: ItemViewHolder<Any>, position: Int) {
        holder.bind(getItem(position), emptyList())
    }

    override fun getItemIdObject(item: Any) = item

    private class HeaderHolder(itemView: View) :
        ItemViewHolder<String>(itemView) {

        override fun bind(header: String, payloads: List<Any>) {
            super.bind(header, payloads)
            itemView.header.text = header
        }
    }

    private inner class BookmarkHolder(itemView: View) :
        SelectionModeViewHolder<Path, Path>(
            itemView,
            selection,
            actionModeProvider,
            actionModeCallback
        ) {

        override fun itemId(file: Path) = file

        override fun bind(path: Path, payloads: List<Any>) {
            super.bind(path, payloads)
            itemView.title.text = FileLabels.get(resources(), path)
            itemView.icon.setImageResource(FileIcons.getDirectory(path))
        }

        override fun onClick(v: View, path: Path) {
            topic.postOnMainThread(OpenFileEvent(path, null))
        }
    }
}
