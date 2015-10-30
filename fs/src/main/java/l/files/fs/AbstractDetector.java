package l.files.fs;

import java.io.IOException;

import static l.files.fs.File.MEDIA_TYPE_OCTET_STREAM;
import static l.files.fs.LinkOption.FOLLOW;

abstract class AbstractDetector {

    // Media types for file types, kept consistent with the linux "file" command
    private static final String INODE_DIRECTORY = "inode/directory";
    private static final String INODE_BLOCKDEVICE = "inode/blockdevice";
    private static final String INODE_CHARDEVICE = "inode/chardevice";
    private static final String INODE_FIFO = "inode/fifo";
    private static final String INODE_SOCKET = "inode/socket";

    String detect(File file) throws IOException {
        return detect(file, file.stat(FOLLOW));
    }

    String detect(File file, Stat stat) throws IOException {
        return tryDetect(file, stat, 100);
    }

    String tryDetect(File file, Stat stat, int tries) throws IOException {
        if (tries <= 0) {
            return MEDIA_TYPE_OCTET_STREAM;
        }
        if (stat.isSymbolicLink()) return tryDetect(file.readLink(), file.stat(FOLLOW), tries - 1);
        if (stat.isRegularFile()) return detectFile(file, stat);
        if (stat.isFifo()) return INODE_FIFO;
        if (stat.isSocket()) return INODE_SOCKET;
        if (stat.isDirectory()) return INODE_DIRECTORY;
        if (stat.isBlockDevice()) return INODE_BLOCKDEVICE;
        if (stat.isCharacterDevice()) return INODE_CHARDEVICE;
        return MEDIA_TYPE_OCTET_STREAM;
    }

    abstract String detectFile(File file, Stat stat) throws IOException;

}