package l.files.util;

import static l.files.util.Files.getFileExtension;

import java.io.File;

import junit.framework.TestCase;

public final class FilesTest extends TestCase {

  public void testGetFileExtensionReturnsEmptyStringIfNoExtension() {
    assertEquals("", getFileExtension(new File("a")));
  }

  public void testGetFileExtensionReturnsExtensionOfFileWithNoNamePart() {
    assertEquals("txt", getFileExtension(new File(".txt")));
  }

  public void testGetFileExtensionReturnsExtensionWithoutDot() {
    assertEquals("txt", getFileExtension(new File("a.txt")));
  }
}
