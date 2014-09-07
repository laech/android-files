package l.files.operations;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static java.util.Arrays.asList;

public final class SizeTest extends FileBaseTest {

  public void testSize() throws Exception {
    File a = tmp().createDir("a");
    File b = tmp().createFile("a/b");
    File c = tmp().createFile("c");
    File d = tmp().createDir("d");

    Size size = new Size(asList(
        a.getPath(), b.getPath(), c.getPath(), d.getPath()));
    size.execute();

    long expected = a.length() + b.length() + c.length() + d.length();
    assertEquals(expected, size.getSize());
  }
}