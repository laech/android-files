package l.files.fs.local;

import com.google.common.net.MediaType;

import java.io.IOException;

import static com.google.common.net.MediaType.OCTET_STREAM;
import static l.files.fs.LinkOption.FOLLOW;

abstract class LocalFileTypeDetector {

    // Media types for file types, kept consistent with the linux "file" command
    private static final MediaType INODE_DIRECTORY = MediaType.parse("inode/directory");
    private static final MediaType INODE_BLOCKDEVICE = MediaType.parse("inode/blockdevice");
    private static final MediaType INODE_CHARDEVICE = MediaType.parse("inode/chardevice");
    private static final MediaType INODE_FIFO = MediaType.parse("inode/fifo");
    private static final MediaType INODE_SOCKET = MediaType.parse("inode/socket");

    /**
     * Detects the content type of a file, if the file is a link returns the
     * content type of the target file.
     */
    public MediaType detect(LocalResource resource) throws IOException {
        return detect(resource.readStatus(FOLLOW)); // TODO option as param?
    }

    /**
     * Detects the content type of a file, use an existing status as hint.
     */
    public MediaType detect(LocalResourceStatus status) throws IOException {
        if (status.isSymbolicLink()) {
            return detect(status.getResource());
        } else {
            if (status.isRegularFile()) return detectRegularFile(status);
            if (status.isFifo()) return INODE_FIFO;
            if (status.isSocket()) return INODE_SOCKET;
            if (status.isDirectory()) return INODE_DIRECTORY;
            if (status.isBlockDevice()) return INODE_BLOCKDEVICE;
            if (status.isCharacterDevice()) return INODE_CHARDEVICE;
            return OCTET_STREAM;
        }
    }

    protected abstract MediaType detectRegularFile(LocalResourceStatus status)
            throws IOException;

}
