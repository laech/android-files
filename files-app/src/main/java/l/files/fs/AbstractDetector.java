package l.files.fs;

import com.google.common.net.MediaType;

import java.io.IOException;

import static com.google.common.net.MediaType.OCTET_STREAM;
import static l.files.fs.LinkOption.FOLLOW;

public abstract class AbstractDetector implements ResourceTypeDetector {

    // Media types for file types, kept consistent with the linux "file" command
    private static final MediaType INODE_DIRECTORY = MediaType.parse("inode/directory");
    private static final MediaType INODE_BLOCKDEVICE = MediaType.parse("inode/blockdevice");
    private static final MediaType INODE_CHARDEVICE = MediaType.parse("inode/chardevice");
    private static final MediaType INODE_FIFO = MediaType.parse("inode/fifo");
    private static final MediaType INODE_SOCKET = MediaType.parse("inode/socket");

    @Override
    public MediaType detect(Resource resource) throws IOException {
        return detect(resource, resource.readStatus(FOLLOW));
    }

    @Override
    public MediaType detect(
            Resource resource, ResourceStatus status) throws IOException {

        if (status.isSymbolicLink()) {
            return detect(resource);
        } else {
            if (status.isRegularFile()) return detectFile(resource, status);
            if (status.isFifo()) return INODE_FIFO;
            if (status.isSocket()) return INODE_SOCKET;
            if (status.isDirectory()) return INODE_DIRECTORY;
            if (status.isBlockDevice()) return INODE_BLOCKDEVICE;
            if (status.isCharacterDevice()) return INODE_CHARDEVICE;
            return OCTET_STREAM;
        }
    }

    protected abstract MediaType detectFile(
            Resource resource, ResourceStatus status) throws IOException;

}
