package l.files.fs.local

import l.files.fs.Path
import l.files.fs.PathEntry
import java.io.IOException
import java.io.File

private data class LocalPathEntry(
        private val _path: LocalPath,
        val ino: Long,
        val isDirectory: Boolean) : PathEntry {

    override fun getPath() = _path

    override fun getResource() = getPath().getResource()

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
