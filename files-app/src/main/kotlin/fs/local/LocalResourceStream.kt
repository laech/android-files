package l.files.fs.local

import l.files.fs.ResourceStream
import kotlin.platform.platformStatic
import java.io.IOException

private class LocalResourceStream private(
        private val parent: LocalPath,
        private val dir: Long) : ResourceStream<LocalPathEntry> {

    /*
     * Design note: this basically uses <dirent.h> to read directory entries,
     * returning simple DirectoryStream.Entry without using stat/lstat will yield
     * much better performance when directory is large.
     */

    private var iterated = false

    override fun iterator() = if (iterated) {
        throw IllegalStateException("iterator() has already been called")
    } else {
        iterated = true
        stream { Dirent.readdir(dir) }
                .filter { !it.isSelfOrParent() }
                .map { it.toEntry() }
                .iterator()
    }

    private fun Dirent.isSelfOrParent() = name == "." || name == ".."

    private fun Dirent.toEntry() = LocalPathEntry(parent.resolve(name),
            inode, type == Dirent.DT_DIR)

    override fun close() {
        try {
            Dirent.closedir(dir)
        } catch (e: ErrnoException) {
            throw e.toIOException()
        }
    }

    public class object {

        platformStatic
        throws(javaClass<IOException>())
        fun open(path: LocalPath) = try {
            LocalResourceStream(path, Dirent.opendir(path.toString()))
        } catch (e: ErrnoException) {
            throw e.toIOException()
        }

    }

}
