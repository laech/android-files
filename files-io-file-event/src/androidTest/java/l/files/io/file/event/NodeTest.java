package l.files.io.file.event;

import l.files.common.testing.FileBaseTest;
import l.files.io.file.FileInfo;

public final class NodeTest extends FileBaseTest {

  public void testCreation() throws Exception {
    String path = tmp().get().getPath();
    FileInfo file = FileInfo.get(path);
    Node node = Node.from(file);
    assertEquals(file.dev(), node.dev());
    assertEquals(file.ino(), node.ino());
  }
}
