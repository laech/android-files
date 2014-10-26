package l.files.fs.local;

import l.files.common.testing.FileBaseTest;

public final class NodeTest extends FileBaseTest {

  public void testCreation() throws Exception {
    String path = tmp().get().getPath();
    LocalFileStatus file = LocalFileStatus.read(path);
    Node node = Node.from(file);
    assertEquals(file.device(), node.dev());
    assertEquals(file.inode(), node.ino());
  }
}
