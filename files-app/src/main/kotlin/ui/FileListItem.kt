package l.files.ui

import l.files.fs.FileStatus
import l.files.fs.Path

abstract class FileListItem() {

    abstract val isFile: Boolean

    val isHeader: Boolean get() = !isFile

    data class Header(private val header: String) : FileListItem() {
        override val isFile = false
        override fun toString() = header
    }

    data class File(val path: Path, val stat: FileStatus?) : FileListItem() {
        override val isFile = true
    }
}
