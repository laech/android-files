package l.files.ui

import l.files.fs.ResourceStatus
import l.files.fs.Path

abstract class FileListItem() {

    abstract val isFile: Boolean

    val isHeader: Boolean get() = !isFile

    data class Header(private val header: String) : FileListItem() {
        override val isFile = false
        override fun toString() = header
    }

    data class File(val path: Path,
                    val stat: ResourceStatus?,
                    private val _targetStat: ResourceStatus?) : FileListItem() {

        override val isFile = true

        /**
         * If the resource is a link, this returns the status of the target
         * file, if not available, returns the status of the link.
         */
        val targetStat: ResourceStatus? get() = _targetStat ?: stat
    }
}
