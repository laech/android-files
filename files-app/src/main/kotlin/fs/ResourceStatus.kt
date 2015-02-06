package l.files.fs

import com.google.common.net.MediaType

trait ResourceStatus : PathEntry {

    val name: String get() = path.name
    val size: Long
    val isRegularFile: Boolean
    val isDirectory: Boolean
    val isSymbolicLink: Boolean
    val isReadable: Boolean
    val isWritable: Boolean
    val isExecutable: Boolean
    val lastModifiedTime: Long
    val basicMediaType: MediaType

}
