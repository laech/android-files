package l.files.fse;

import junit.framework.TestCase;

import l.files.io.os.Stat;

public final class NodeTest extends TestCase {

  public void testCreation() {
    long dev = 1;
    long ino = 2;
    Stat stat = new Stat(dev, ino, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    Node node = Node.from(stat);
    assertEquals(dev, node.dev());
    assertEquals(ino, node.ino());
  }
}
