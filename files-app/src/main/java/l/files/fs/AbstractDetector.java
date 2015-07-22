package l.files.fs;

import java.io.IOException;

import static l.files.fs.LinkOption.FOLLOW;

abstract class AbstractDetector implements Detector {

  // Media types for file types, kept consistent with the linux "file" command
  private static final String INODE_DIRECTORY = "inode/directory";
  private static final String INODE_BLOCKDEVICE = "inode/blockdevice";
  private static final String INODE_CHARDEVICE = "inode/chardevice";
  private static final String INODE_FIFO = "inode/fifo";
  private static final String INODE_SOCKET = "inode/socket";

  @Override
  public String detect(Resource resource) throws IOException {
    return detect(resource, resource.stat(FOLLOW));
  }

  @Override
  public String detect(Resource resource, Stat stat) throws IOException {
    if (stat.isSymbolicLink()) {
      return detect(resource);
    } else {
      if (stat.isRegularFile()) return detectFile(resource, stat);
      if (stat.isFifo()) return INODE_FIFO;
      if (stat.isSocket()) return INODE_SOCKET;
      if (stat.isDirectory()) return INODE_DIRECTORY;
      if (stat.isBlockDevice()) return INODE_BLOCKDEVICE;
      if (stat.isCharacterDevice()) return INODE_CHARDEVICE;
      return OCTET_STREAM;
    }
  }

  abstract String detectFile(Resource resource, Stat stat) throws IOException;

}
