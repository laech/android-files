package l.files.operations

import java.io.IOException
import java.nio.channels.ClosedByInterruptException

import l.files.fs.Path
import l.files.fs.ResourceStatus
import l.files.logging.Logger

import l.files.fs.Resource.TraversalOrder.PRE_ORDER

private class Copy(sources: Iterable<Path>, dstDir: Path) : Paste(sources, dstDir) {

    volatile var copiedByteCount = 0L
        private set

    volatile var copiedItemCount = 0
        private set

    override fun paste(from: Path, to: Path, listener: FailureRecorder) {

        try {

            from.resource.traverse(PRE_ORDER) { res, err ->
                listener.onFailure(res.path, err)
            }

        } catch (e: IOException) {
            listener.onFailure(from, e)
            return

        }.forEach {
            checkInterrupt()

            val path = it.path
            val status = try {
                it.resource.readStatus(false)
            } catch (e: IOException) {
                listener.onFailure(path, e)
                return
            }

            val dst = path.replace(from, to)
            if (status.isSymbolicLink) {
                copyLink(status, dst, listener)
            } else if (status.isDirectory) {
                createDirectory(status, dst, listener)
            } else if (status.isRegularFile) {
                copyFile(status, dst, listener)
            } else {
                listener.onFailure(path, IOException("Not a file or directory"))
            }
        }
    }

    private fun copyLink(src: ResourceStatus, dst: Path, listener: FailureRecorder) {
        try {
            dst.resource.createSymbolicLink(src.resource.readSymbolicLink())
            copiedByteCount += src.size
            copiedItemCount++
            setLastModifiedDate(src, dst)
        } catch (e: IOException) {
            listener.onFailure(src.path, e)
        }

    }

    private fun createDirectory(src: ResourceStatus, dst: Path, listener: FailureRecorder) {
        try {
            dst.resource.createDirectory()
            copiedByteCount += src.size
            copiedItemCount++
            setLastModifiedDate(src, dst)
        } catch (e: IOException) {
            listener.onFailure(src.path, e)
        }
    }

    private fun copyFile(src: ResourceStatus, dst: Path, listener: FailureRecorder) {
        checkInterrupt()

        try {

            src.resource.openInputStream().use { source ->
                dst.resource.openOutputStream().use { sink ->
                    val buf = ByteArray(BUFFER_SIZE)
                    var n = source.read(buf)
                    while (n > 0) {
                        sink.write(buf, 0, n)
                        copiedByteCount += n
                        n = source.read(buf)
                    }
                    copiedItemCount++
                }
            }

        } catch (e: IOException) {
            try {
                dst.resource.delete()
            } catch (e: IOException) {
                logger.warn(e, "Failed to delete path on exception %s", dst)
            }

            if (e is ClosedByInterruptException) {
                throw InterruptedException()
            } else {
                listener.onFailure(src.path, e)
            }
        }

        setLastModifiedDate(src, dst)
    }

    private fun setLastModifiedDate(src: ResourceStatus, dst: Path) {
        try {
            dst.resource.setLastModifiedTime(src.lastModifiedTime)
        } catch (e: IOException) {
            logger.debug(e, "Failed to set last modified date on %s", dst);
        }
        /*
         * Setting last modified time currently fails, see:
         *
         * https://code.google.com/p/android/issues/detail?id=18624
         * https://code.google.com/p/android/issues/detail?id=34691
         * https://code.google.com/p/android/issues/detail?id=1992
         * https://code.google.com/p/android/issues/detail?id=1699
         * https://code.google.com/p/android/issues/detail?id=25460
         *
         * So comment this log out, since it always fails.
         */
    }

    companion object {

        private val logger = Logger.get(javaClass<Copy>())

        private val BUFFER_SIZE = 1024 * 8

    }

}
