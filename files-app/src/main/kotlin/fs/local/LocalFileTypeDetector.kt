package l.files.fs.local

import com.google.common.net.MediaType

import l.files.fs.ResourceStatus
import l.files.fs.FileTypeDetector
import l.files.fs.Path
import l.files.logging.Logger

import com.google.common.net.MediaType.OCTET_STREAM

private abstract class LocalFileTypeDetector : FileTypeDetector {

    override fun detect(path: Path) =
            detectLocal((path as LocalPath).resource.readStatus(true))

    override fun detect(status: ResourceStatus) = if (status.isSymbolicLink) {
        detect(status.path)
    } else {
        detectLocal(status as LocalResourceStatus)
    }

    private fun detectLocal(status: LocalResourceStatus): MediaType {
        if (status.isSymbolicLink) return detectLocal(status.resource.readStatus(true))
        if (status.isRegularFile) return detectRegularFile(status)
        if (status.isFifo) return INODE_FIFO
        if (status.isSocket) return INODE_SOCKET
        if (status.isDirectory) return INODE_DIRECTORY
        if (status.isBlockDevice) return INODE_BLOCKDEVICE
        if (status.isCharacterDevice) return INODE_CHARDEVICE
        return OCTET_STREAM
    }

    protected abstract fun detectRegularFile(status: ResourceStatus): MediaType

    class object {

        private val log = Logger.get(javaClass<LocalFileTypeDetector>())

        // Media types for file types, kept consistent with the linux "file" command
        private val INODE_DIRECTORY = MediaType.parse("inode/directory")
        private val INODE_BLOCKDEVICE = MediaType.parse("inode/blockdevice")
        private val INODE_CHARDEVICE = MediaType.parse("inode/chardevice")
        private val INODE_FIFO = MediaType.parse("inode/fifo")
        private val INODE_SYMLINK = MediaType.parse("inode/symlink")
        private val INODE_SOCKET = MediaType.parse("inode/socket")

    }

}

