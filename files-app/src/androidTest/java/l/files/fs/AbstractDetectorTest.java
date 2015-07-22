package l.files.fs;

import java.io.IOException;
import java.io.Writer;

import l.files.fs.local.ResourceBaseTest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class AbstractDetectorTest extends ResourceBaseTest {

  /**
   * The detector to be tested, using the given file system.
   */
  abstract AbstractDetector detector();

  public void test_detects_directory_type() throws Exception {
    Resource dir = dir1().resolve("a").createDirectory();
    assertEquals("inode/directory", detector().detect(dir).toString());
  }

  public void test_detects_file_type() throws Exception {
    Resource file = createTextFile("a.txt");
    assertEquals("text/plain", detector().detect(file).toString());
  }

  public void test_detects_linked_file_type() throws Exception {
    Resource file = createTextFile("a.mp3");
    Resource link = dir1().resolve("b.txt").createLink(file);
    assertEquals("text/plain", detector().detect(link).toString());
  }

  private Resource createTextFile(String name) throws IOException {
    Resource file = dir1().resolve(name).createFile();
    try (Writer writer = file.writer(NOFOLLOW, UTF_8)) {
      writer.write("hello world");
    }
    return file;
  }

  public void test_detects_linked_directory_type() throws Exception {
    Resource dir = dir1().resolve("a").createDirectory();
    Resource link = dir1().resolve("b").createLink(dir);
    assertEquals("inode/directory", detector().detect(link).toString());
  }

  public void test_detects_multi_linked_directory_type() throws Exception {
    Resource dir = dir1().resolve("a").createDirectory();
    Resource link1 = dir1().resolve("b").createLink(dir);
    Resource link2 = dir1().resolve("c").createLink(link1);
    assertEquals("inode/directory", detector().detect(link2).toString());
  }

}
