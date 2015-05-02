package l.files.fs;

import com.google.common.net.MediaType;

import java.io.IOException;

import static com.google.common.net.MediaType.OCTET_STREAM;
import static l.files.fs.LinkOption.FOLLOW;

public abstract class AbstractDetector implements Detector
{
    // Media types for file types, kept consistent with the linux "file" command
    private static final MediaType INODE_DIRECTORY = MediaType.parse("inode/directory");
    private static final MediaType INODE_BLOCKDEVICE = MediaType.parse("inode/blockdevice");
    private static final MediaType INODE_CHARDEVICE = MediaType.parse("inode/chardevice");
    private static final MediaType INODE_FIFO = MediaType.parse("inode/fifo");
    private static final MediaType INODE_SOCKET = MediaType.parse("inode/socket");

    @Override
    public MediaType detect(final Resource resource) throws IOException
    {
        return detect(resource, resource.stat(FOLLOW));
    }

    @Override
    public MediaType detect(final Resource resource, final Stat stat)
            throws IOException
    {
        if (stat.isSymbolicLink())
        {
            return detect(resource);
        }
        else
        {
            if (stat.isRegularFile()) return detectFile(resource, stat);
            if (stat.isFifo()) return INODE_FIFO;
            if (stat.isSocket()) return INODE_SOCKET;
            if (stat.isDirectory()) return INODE_DIRECTORY;
            if (stat.isBlockDevice()) return INODE_BLOCKDEVICE;
            if (stat.isCharacterDevice()) return INODE_CHARDEVICE;
            return OCTET_STREAM;
        }
    }

    protected abstract MediaType detectFile(Resource resource, Stat stat)
            throws IOException;
}
