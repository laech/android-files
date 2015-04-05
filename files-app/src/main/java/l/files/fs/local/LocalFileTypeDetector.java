package l.files.fs.local;

import com.google.common.net.MediaType;

import java.io.IOException;

import l.files.fs.FileTypeDetector;
import l.files.fs.Path;
import l.files.fs.ResourceStatus;

import static com.google.common.net.MediaType.OCTET_STREAM;

abstract class LocalFileTypeDetector implements FileTypeDetector {

    // Media types for file types, kept consistent with the linux "file" command
    private static final MediaType INODE_DIRECTORY = MediaType.parse("inode/directory");
    private static final MediaType INODE_BLOCKDEVICE = MediaType.parse("inode/blockdevice");
    private static final MediaType INODE_CHARDEVICE = MediaType.parse("inode/chardevice");
    private static final MediaType INODE_FIFO = MediaType.parse("inode/fifo");
    private static final MediaType INODE_SYMLINK = MediaType.parse("inode/symlink");
    private static final MediaType INODE_SOCKET = MediaType.parse("inode/socket");

    @Override
    public MediaType detect(Path path) throws IOException {
        return detectLocal((LocalResourceStatus) path.getResource().readStatus(true));
    }

    @Override
    public MediaType detect(ResourceStatus status) throws IOException {
        if (status.isSymbolicLink()) {
            return detect(status.getPath());
        } else {
            return detectLocal((LocalResourceStatus) status);
        }
    }

    private MediaType detectLocal(LocalResourceStatus status) throws IOException {
        if (status.isSymbolicLink()) return detectLocal(status.getResource().readStatus(true));
        if (status.isRegularFile()) return detectRegularFile(status);
        if (status.isFifo()) return INODE_FIFO;
        if (status.isSocket()) return INODE_SOCKET;
        if (status.isDirectory()) return INODE_DIRECTORY;
        if (status.isBlockDevice()) return INODE_BLOCKDEVICE;
        if (status.isCharacterDevice()) return INODE_CHARDEVICE;
        return OCTET_STREAM;
    }

    protected abstract MediaType detectRegularFile(ResourceStatus status) throws IOException;

}
