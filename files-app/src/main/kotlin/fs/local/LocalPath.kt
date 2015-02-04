package l.files.fs.local

import android.os.Parcel
import android.os.Parcelable.Creator

import java.io.File
import java.net.URI

import l.files.fs.Path
import kotlin.platform.platformStatic

private data class LocalPath private(val file: File) : Path {

    override val resource = LocalResource(this)

    override val uri = file.sanitizedUri()

    override fun startsWith(other: Path): Boolean {
        if (other.parent == null || other == this) {
            return true
        }
        if (other is LocalPath) {
            val thisPath = file.path
            val thatPath = other.file.path
            return thisPath.startsWith(thatPath) &&
                    thisPath.charAt(thatPath.length()) == '/'
        }
        return false
    }

    override val parent = if (file.path == "/") {
        null
    } else {
        LocalPath(file.getParentFile())
    }

    override val name = file.name

    override fun resolve(other: String) = File(file, other).toLocalPath()

    override fun writeToParcel(dst: Parcel, flags: Int) {
        dst.writeString(file.path)
    }

    override fun describeContents() = 0

    override fun toString() = file.path

    class object {

        public val CREATOR: Creator<LocalPath> = object : Creator<LocalPath> {
            override fun createFromParcel(source: Parcel) = File(source.readString()).toLocalPath()
            override fun newArray(size: Int) = arrayOfNulls<LocalPath?>(size)
        }

        /**
         * If the given path is an instance of this class,
         * throws IllegalArgumentException if it's not.
         */
        platformStatic
        fun check(path: Path): LocalPath {
            if (path is LocalPath) {
                return path
            } else {
                throw IllegalArgumentException(path.uri.toString())
            }
        }

        platformStatic
        fun of(file: File) = file.toLocalPath()

        platformStatic
        fun of(path: String) = of(File(path))

        private fun File.toLocalPath() = LocalPath(File(this.sanitizedUri()))

        private fun File.sanitizedUri(): URI {
            /*
             * Don't return File.toURI as it will append a "/" to the end of the URI
             * depending on whether or not the file is a directory, that means two
             * calls to the method before and after the directory is deleted will
             * create two URIs that are not equal.
             */
            val uri = toURI().normalize()
            val uriStr = uri.toString()
            if (uri.getRawPath() != "/" && uriStr.endsWith("/")) {
                return URI.create(uriStr.substring(0, uriStr.length() - 1))
            }
            return uri
        }
    }
}
