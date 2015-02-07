package l.files.fs.local;

import l.files.common.testing.FileBaseTest;

public final class NodeTest extends FileBaseTest {

  public void testCreation() throws Exception {
    LocalResourceStatus file = LocalResourceStatus.stat(tmp().get(), false);
    Node node = Node.from(file);
    assertEquals(file.getDevice(), node.getDevice());
    assertEquals(file.getInode(), node.getInode());
  }
}
