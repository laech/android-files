package l.files.fs.local;

import com.google.auto.value.AutoValue;

/**
 * inode numbers are not unique across devices, this class exists to facilitate
 * that by pairing an inode number with the device ID.
 */
@AutoValue
abstract class Node {
  Node() {}

  public static Node from(LocalFileStatus file) {
    return create(file.device(), file.inode());
  }

  public static Node create(long dev, long ino) {
    return new AutoValue_Node(dev, ino);
  }

  public abstract long dev();
  public abstract long ino();
}
