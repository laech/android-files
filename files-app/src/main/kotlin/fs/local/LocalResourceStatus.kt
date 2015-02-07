package l.files.fs.local

import java.io.File
import java.io.IOException

import l.files.fs.ResourceStatus
import l.files.fs.Path

import kotlin.properties.Delegates
import kotlin.platform.platformStatic
import com.google.common.net.MediaType

private data class LocalResourceStatus private (
        override val path: LocalPath,
        val stat: Stat) : ResourceStatus {

    override val resource: LocalResource get() = path.resource

    override val isReadable: Boolean by Delegates.lazy { access(Unistd.R_OK) }
    override val isWritable: Boolean by Delegates.lazy { access(Unistd.W_OK) }
    override val isExecutable: Boolean by Delegates.lazy { access(Unistd.X_OK) }

    private fun access(mode: Int) = try {
        Unistd.access(path.toString(), mode)
        true
    } catch (e: ErrnoException) {
        false
    }

    override val lastModifiedTime = stat.mtime() * 1000
    override val size = stat.size()

    override val isSymbolicLink = Stat.S_ISLNK(stat.mode())
    override val isRegularFile = Stat.S_ISREG(stat.mode())
    override val isDirectory = Stat.S_ISDIR(stat.mode())

    val device = stat.dev()
    val inode = stat.ino()

    val isFifo = Stat.S_ISFIFO(stat.mode())
    val isSocket = Stat.S_ISSOCK(stat.mode())
    val isBlockDevice = Stat.S_ISBLK(stat.mode())
    val isCharacterDevice = Stat.S_ISCHR(stat.mode())

    override val basicMediaType: MediaType by Delegates.lazy {
        try {
            BasicFileTypeDetector.detect(this)
        } catch (e: IOException) {
            MediaType.OCTET_STREAM
        }
    }

    public class object {

        platformStatic
        throws(javaClass<IOException>())
        fun stat(file: File, followLink: Boolean) =
                stat(LocalPath.of(file), followLink)

        platformStatic
        throws(javaClass<IOException>())
        fun stat(path: Path, followLink: Boolean): LocalResourceStatus {
            LocalPath.check(path)
            val stat: Stat
            try {
                if (followLink) {
                    stat = Stat.stat(path.toString())
                } else {
                    stat = Stat.lstat(path.toString())
                }
            } catch (e: ErrnoException) {
                throw e.toIOException()
            }

            return LocalResourceStatus(path as LocalPath, stat)
        }
    }
}
