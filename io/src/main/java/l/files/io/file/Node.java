package l.files.io.file;

import com.google.auto.value.AutoValue;

/**
 * inode numbers are not unique across devices, this class exists to facilitate
 * that by pairing an inode number with the device ID.
 */
@AutoValue
abstract class Node {
  Node() {}

  public static Node from(FileInfo file) {
    return new AutoValue_Node(file.getDeviceId(), file.getInodeNumber());
  }

  public abstract long dev();
  public abstract long ino();
}