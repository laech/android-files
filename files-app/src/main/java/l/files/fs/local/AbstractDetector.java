package l.files.fs.local;

import java.io.IOException;

import l.files.fs.File;

import static l.files.fs.File.OCTET_STREAM;
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

    String detect(File file, l.files.fs.Stat stat) throws IOException {
        if (stat.isSymbolicLink()) {
            return detect(file);
        } else {
            if (stat.isRegularFile()) return detectFile(file, stat);
            if (stat.isFifo()) return INODE_FIFO;
            if (stat.isSocket()) return INODE_SOCKET;
            if (stat.isDirectory()) return INODE_DIRECTORY;
            if (stat.isBlockDevice()) return INODE_BLOCKDEVICE;
            if (stat.isCharacterDevice()) return INODE_CHARDEVICE;
            return OCTET_STREAM;
        }
    }

    abstract String detectFile(File file, l.files.fs.Stat stat) throws IOException;

}
