package l.files.io.file;

import l.files.common.testing.FileBaseTest;

public final class NodeTest extends FileBaseTest {

  public void testCreation() throws Exception {
    String path = tmp().get().getPath();
    FileInfo file = FileInfo.read(path);
    Node node = Node.from(file);
    assertEquals(file.device(), node.dev());
    assertEquals(file.inode(), node.ino());
  }
}
