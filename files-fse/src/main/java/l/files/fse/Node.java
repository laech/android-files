package l.files.fse;

import com.google.common.base.Objects;

import l.files.os.Stat;

/**
 * inode numbers are not unique across devices, this class exists to facilitate
 * that by pairing an inode number with the device ID.
 */
final class Node {

  public final long dev;
  public final long ino;

  public Node(long dev, long inode) {
    this.dev = dev;
    this.ino = inode;
  }

  public static Node from(Stat stat) {
    return new Node(stat.dev, stat.ino);
  }

  @Override public int hashCode() {
    return Objects.hashCode(dev, ino);
  }

  @Override public boolean equals(Object o) {
    if (o instanceof Node) {
      Node that = (Node) o;
      return dev == that.dev
          && ino == that.ino;
    }
    return false;
  }
}
