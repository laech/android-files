package l.files.fs.local

import android.os.Parcel
import android.os.Parcelable.Creator
import com.google.common.collect.TreeTraverser
import l.files.fs.*
import l.files.fs.Resource.TraversalOrder
import l.files.fs.Resource.TraversalOrder.BREATH_FIRST
import l.files.fs.Resource.TraversalOrder.POST_ORDER
import l.files.fs.Resource.TraversalOrder.PRE_ORDER
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

private data class LocalResource(private val _path: LocalPath) : Resource {

    override fun getPath() = _path

    override fun getResource() = getPath().getResource()

    override val watcher: WatchService get() = LocalWatchService.get()

    override fun resolve(other: String) = LocalResource(getPath().resolve(other))

    override fun readStatus(followLink: Boolean) = LocalResourceStatus.stat(getPath(), followLink)

    override val exists: Boolean get() = try {
        Unistd.access(getPath().toString(), Unistd.F_OK)
        true
    } catch (e: ErrnoException) {
        false
    }

    override fun traverse(
            order: TraversalOrder,
            handler: (Resource, IOException) -> Unit): Sequence<LocalResource> {

        val root = LocalPathEntry.read(getPath())
        val fn: (PathEntry, IOException) -> Unit = { entry, exception ->
            handler(entry.getResource(), exception)
        }
        return when (order) {
            BREATH_FIRST -> Traverser(fn).breadthFirstTraversal(root)
            POST_ORDER -> Traverser(fn).postOrderTraversal(root)
            PRE_ORDER -> Traverser(fn).preOrderTraversal(root)
        }.sequence().map { it.getResource() }
    }

    private class Traverser(private val handler: (PathEntry, IOException) -> Unit) : TreeTraverser<LocalPathEntry>() {

        override fun children(root: LocalPathEntry) = try {
            if (root.isDirectory) {
                LocalResourceStream.open(root.getPath()).use { it.toArrayList() }
            } else {
                emptyList<LocalPathEntry>()
            }
        } catch (e: IOException) {
            handler(root, e)
            emptyList<LocalPathEntry>()
        }
    }

    override fun openResourceStream() = object : ResourceStream<LocalResource> {
        val stream = LocalResourceStream.open(getPath())
        override fun iterator() = stream.map { it.getResource() }.iterator()
        override fun close() {
            stream.close()
        }
    }

    override fun openInputStream() = FileInputStream(getPath().toString())

    override fun openOutputStream() = FileOutputStream(getPath().toString())

    override fun createDirectory() {
        createDirectory(getPath())
    }

    private fun createDirectory(path: LocalPath) {
        if (!getPath().parent!!.getResource().exists) {
            createDirectory(getPath().parent)
        }
        val f = java.io.File(getPath().toString())
        if (!f.isDirectory() && !f.mkdir()) {
            throw IOException() // TODO use native code to get errno
        }
    }

    override fun createFile() {
        if (!java.io.File(getPath().toString()).createNewFile()) {
            throw IOException() // TODO use native code to get errno
        }
    }

    override fun createSymbolicLink(target: Path) {
        LocalPath.check(target)
        try {
            Unistd.symlink(target.toString(), getPath().toString())
        } catch (e: ErrnoException) {
            throw e.toIOException()
        }
    }

    override fun readSymbolicLink() = try {
        LocalResource(LocalPath.of(Unistd.readlink(getPath().toString())))
    } catch (e: ErrnoException) {
        throw e.toIOException()
    }

    override fun move(dst: Path) {
        LocalPath.check(dst)
        try {
            Stdio.rename(getPath().toString(), dst.toString())
        } catch (e: ErrnoException) {
            throw e.toIOException()
        }
    }

    override fun delete() {
        try {
            Stdio.remove(getPath().toString());
        } catch (e: ErrnoException) {
            throw e.toIOException()
        }
    }

    override fun setLastModifiedTime(time: Long) {
        if (!java.io.File(getPath().toString()).setLastModified(time)) {
            throw IOException() // TODO use native code to get errno
        }
    }

    override fun detectMediaType() = MagicFileTypeDetector.detect(getPath())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(getPath(), 0)
    }

    companion object {

        public val CREATOR: Creator<LocalResource> = object : Creator<LocalResource> {
            override fun newArray(size: Int) = arrayOfNulls<LocalResource>(size)
            override fun createFromParcel(source: Parcel) = LocalResource(
                    source.readParcelable(javaClass.getClassLoader()))
        }

    }
}
