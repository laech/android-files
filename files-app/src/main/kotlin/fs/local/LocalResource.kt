package l.files.fs.local

import java.io.FileInputStream
import java.io.IOException

import l.files.fs.Path
import l.files.fs.Resource
import l.files.fs.WatchService
import android.os.Parcel
import android.os.Parcelable.Creator

private data class LocalResource(override val path: LocalPath) : Resource {

    override val resource: LocalResource get() = path.resource

    override val watcher: WatchService get() = LocalWatchService.get()

    override fun resolve(other: String) = LocalResource(path.resolve(other))

    override fun readStatus(followLink: Boolean) = LocalResourceStatus.stat(path, followLink)

    override val exists: Boolean get() = try {
        Unistd.access(path.toString(), Unistd.F_OK)
        true
    } catch (e: ErrnoException) {
        false
    }

    override fun newDirectoryStream() = LocalDirectoryStream.open(path)

    override fun newInputStream() = FileInputStream(path.toString())

    override fun createDirectory() {
        createDirectory(path)
    }

    private fun createDirectory(path: LocalPath) {
        if (!path.parent!!.resource.exists) {
            createDirectory(path.parent)
        }
        val f = java.io.File(path.toString())
        if (!f.isDirectory() && !f.mkdir()) {
            throw IOException() // TODO use native code to get errno
        }
    }

    override fun createFile() {
        if (!java.io.File(path.toString()).createNewFile()) {
            throw IOException() // TODO use native code to get errno
        }
    }

    override fun createSymbolicLink(target: Path) {
        LocalPath.check(target)
        Unistd.symlink(target.toString(), path.toString())
    }

    override fun readSymbolicLink() = try {
        LocalResource(LocalPath.of(Unistd.readlink(path.toString())))
    } catch (e: ErrnoException) {
        throw e.toIOException()
    }

    override fun move(dst: Path) {
        LocalPath.check(dst)
        Stdio.rename(path.toString(), dst.toString())
    }

    override fun detectMediaType() = MagicFileTypeDetector.detect(path)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(path, 0)
    }

    class object {

        public val CREATOR: Creator<LocalResource> = object : Creator<LocalResource> {
            override fun newArray(size: Int) = arrayOfNulls<LocalResource>(size)
            override fun createFromParcel(source: Parcel) = LocalResource(
                    source.readParcelable(javaClass.getClassLoader()))
        }

    }
}
