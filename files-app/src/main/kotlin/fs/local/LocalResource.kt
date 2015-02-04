package l.files.fs.local

import com.google.common.net.MediaType

import org.apache.tika.Tika

import java.io.FileInputStream
import java.io.IOException

import l.files.fs.Path
import l.files.fs.Resource
import l.files.fs.WatchService
import kotlin.properties.Delegates
import android.os.Parcel
import android.os.Parcelable.Creator

private data class LocalResource(override val path: LocalPath) : Resource {

    override val watcher: WatchService get() = LocalWatchService.get()

    override fun resolve(other: String) = LocalResource(path.resolve(other))

    override fun stat() = LocalFileStatus.stat(path, false)

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
        if (path.resource.exists) {
            return
        }
        createDirectory(path.parent!!)
        if (!java.io.File(path.toString()).mkdir()) {
            throw IOException() // TODO use native code to get errno
        }
    }

    override fun move(dst: Path) {
        assert(dst is LocalPath)
        try {
            Stdio.rename(path.toString(), dst.toString())
        } catch (e: ErrnoException) {
            throw e.toFileSystemException() // TODO
        }
    }

    override fun detectMediaType(): MediaType {
        newInputStream().use { `in` -> return MediaType.parse(tika.detect(`in`)) }
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(path, 0)
    }

    class object {

        private val tika: Tika by Delegates.lazy { Tika() }

        public val CREATOR: Creator<LocalResource> = object : Creator<LocalResource> {
            override fun newArray(size: Int) = arrayOfNulls<LocalResource>(size)
            override fun createFromParcel(source: Parcel) = LocalResource(
                    source.readParcelable(javaClass.getClassLoader()))
        }

    }
}
