package l.files.fse;

import com.google.auto.value.AutoValue;

import l.files.io.os.Stat;

/**
 * inode numbers are not unique across devices, this class exists to facilitate
 * that by pairing an inode number with the device ID.
 */
@AutoValue
abstract class Node {
  Node() {}

  public static Node from(Stat stat) {
    return new AutoValue_Node(stat.dev, stat.ino);
  }

  public abstract long dev();
  public abstract long ino();
}
