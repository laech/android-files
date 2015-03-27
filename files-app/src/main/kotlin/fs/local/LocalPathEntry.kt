package l.files.fs.local

import l.files.fs.PathEntry
import java.io.IOException
import java.io.File

private data class LocalPathEntry(
        override val path: LocalPath,
        val ino: Long,
        val isDirectory: Boolean) : PathEntry {

    override val resource = path.resource

    public companion object {

        throws(javaClass<IOException>())
        fun stat(file: File): LocalPathEntry {
            return read(LocalPath.of(file))
        }

        throws(javaClass<IOException>())
        fun read(path: LocalPath): LocalPathEntry {
            val status = LocalResourceStatus.stat(path, false)
            return LocalPathEntry(path, status.inode, status.isDirectory)
        }

    }

}
