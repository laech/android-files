package l.files.fs.media;

import android.content.Context;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.fs.Files.readSymbolicLink;
import static l.files.fs.Files.stat;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.media.MediaTypes.MEDIA_TYPE_OCTET_STREAM;

abstract class BasePropertyDetector {

    // Media types for file types, kept consistent with the linux "file" command
    private static final String INODE_DIRECTORY = "inode/directory";
    private static final String INODE_BLOCKDEVICE = "inode/blockdevice";
    private static final String INODE_CHARDEVICE = "inode/chardevice";
    private static final String INODE_FIFO = "inode/fifo";
    private static final String INODE_SOCKET = "inode/socket";

    String detect(Context context, Path path) throws IOException {
        return detect(context, path, stat(path, FOLLOW));
    }

    String detect(Context context, Path path, Stat stat) throws IOException {
        if (stat.isSymbolicLink()) {
            return detect(
                    context,
                    readSymbolicLink(path),
                    stat(path, FOLLOW));
        }
        if (stat.isRegularFile()) return detectFile(context, path, stat);
        if (stat.isFifo()) return INODE_FIFO;
        if (stat.isSocket()) return INODE_SOCKET;
        if (stat.isDirectory()) return INODE_DIRECTORY;
        if (stat.isBlockDevice()) return INODE_BLOCKDEVICE;
        if (stat.isCharacterDevice()) return INODE_CHARDEVICE;
        return MEDIA_TYPE_OCTET_STREAM;
    }

    abstract String detectFile(Context context, Path path, Stat stat) throws IOException;

}
