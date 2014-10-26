package l.files.fs.local;

import l.files.common.testing.FileBaseTest;
import l.files.fs.local.FileInfo;
import l.files.fs.local.Node;

public final class NodeTest extends FileBaseTest {

  public void testCreation() throws Exception {
    String path = tmp().get().getPath();
    FileInfo file = FileInfo.read(path);
    Node node = Node.from(file);
    assertEquals(file.device(), node.dev());
    assertEquals(file.inode(), node.ino());
  }
}
